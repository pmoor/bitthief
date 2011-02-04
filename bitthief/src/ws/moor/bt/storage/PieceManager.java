/*
 * BitThief - A Free Riding BitTorrent Client
 * Copyright (C) 2006 Patrick Moor <patrick@moor.ws>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */

package ws.moor.bt.storage;

import org.apache.log4j.Logger;
import ws.moor.bt.Environment;
import ws.moor.bt.downloader.Block;
import ws.moor.bt.stats.CounterRepository;
import ws.moor.bt.torrent.MetaInfo;
import ws.moor.bt.util.ArrayUtil;
import ws.moor.bt.util.LoggingUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class PieceManager implements DataBlockProvider {

  private final MetaInfo metaInfo;
  private final Storage storage;

  private ValidPiecesStrategy validPieces;
  private final PartialPieceContainer partialPieces;

  private final List<PieceListener> listeners = new ArrayList<PieceListener>();

  private static final Logger logger = LoggingUtil.getLogger(PieceManager.class);

  private final Environment environment;
  private final CounterRepository counterRepository;

  public PieceManager(MetaInfo metaInfo,
                      Storage storage,
                      Environment environment,
                      CounterRepository counterRepository) {
    assertStorageMatchesMetaInfo(metaInfo, storage);
    this.metaInfo = metaInfo;
    this.storage = storage;
    this.environment = environment;
    this.counterRepository = counterRepository;
    validPieces = new ScanFileValidPiecesStrategy(storage, metaInfo);
    partialPieces = new PartialPieceContainer();
  }

  private void assertStorageMatchesMetaInfo(MetaInfo metaFile, Storage storage) {
    if (metaFile.getPieceCount() != storage.getPieceCount()) {
      throw new IllegalArgumentException("storage size and metainfo piece count do not match");
    }
  }

  public boolean isPieceAvailable(int index) {
    return validPieces.hasPiece(index);
  }

  public DataBlock getBlock(Block block) {
    assertValidOffsets(block);
    assertAvailablePiece(block);
    byte[] pieceBuffer = new byte[storage.getPieceLength(block.getPieceIndex())];
    storage.readPiece(block.getPieceIndex(), pieceBuffer);
    return new DataBlock(block.getPieceIndex(),
        block.getOffset(), ArrayUtil.subArray(pieceBuffer, block.getOffset(), block.getLength()));
  }

  public synchronized void setBlock(DataBlock block) {
    assertValidOffsets(block);
    if (validPieces.hasPiece(block.getPieceIndex())) {
      logger.warn("tried to add a block (" + block +
          "), but the piece is already available");
      counterRepository.getCounter("torrent.blocks.duplicates").increase(1);
      return;
    }

    partialPieces.addBlock(block);
    byte[] piece = partialPieces.checkPieceForCompleteness(
        block.getPieceIndex(), storage.getPieceLength(block.getPieceIndex()));
    if (piece != null) {
      if (metaInfo.getPieceHashes()[block.getPieceIndex()].equalsHashOf(piece, 0, piece.length)) {
        addValidPiece(block.getPieceIndex(), piece);
      } else {
        logger.warn("hash does not match for piece " + block.getPieceIndex());
        counterRepository.getCounter("torrent.pieces.hashfailed").increase(1);
      }
    }

    counterRepository.getCounter("torrent.pieces.pending").set(
        partialPieces.getPartiallyAvailablePiecesCount());
  }

  private void addValidPiece(int pieceIndex, byte[] piece) {
    logger.info("got valid piece " + pieceIndex);
    storage.writePiece(pieceIndex, piece);
    validPieces.gotPiece(pieceIndex);
    BitField valids = validPieces.getValidPieces();
    logger.info(valids.getAvailablePieceCount() + "/" + valids.getPieceCount() + " pieces available");
    counterRepository.getCounter("torrent.validbytes").increase(piece.length);
    counterRepository.getCounter("torrent.pieces.valid").increase(1);
    notifyPieceListenersAboutValidPiece(pieceIndex);
  }

  public List<Integer> getPartiallyAvailablePieces() {
    return partialPieces.getPartiallyAvailablePieces();
  }

  public List<Block> getMissingBlocks(int pieceIndex, int maxBlockLength) {
    if (validPieces.hasPiece(pieceIndex)) {
      return Arrays.asList();
    }
    return partialPieces.getMissingBlocks(pieceIndex, storage.getPieceLength(pieceIndex), maxBlockLength);
  }

  private void assertAvailablePiece(Block block) {
    if (!validPieces.hasPiece(block.getPieceIndex())) {
      throw new IllegalArgumentException("this piece is not available");
    }
  }

  private void assertValidOffsets(Block block) {
    if (block.getPieceIndex() < 0 || block.getPieceIndex() >= storage.getPieceCount()) {
      throw new IllegalArgumentException("invalid piece index: " + block.getPieceIndex());
    }
    int pieceLength = storage.getPieceLength(block.getPieceIndex());
    if (block.getOffset() < 0 || block.getOffset() >= pieceLength) {
      throw new IllegalArgumentException("invalid offset: " + block.getOffset());
    }
    if (block.getLength() < 0 || block.getOffset() + block.getLength() > pieceLength) {
      throw new IllegalArgumentException("invalid length: " + block.getLength());
    }
  }

  public BitField getValidPieces() {
    return validPieces.getValidPieces();
  }

  public boolean isBitFieldOfInterest(BitField field) {
    if (field == null) {
      return false;
    }
    return field.minus(getValidPieces()).getAvailablePieceCount() > 0;
  }

  private void notifyPieceListenersAboutValidPiece(final int pieceIndex) {
    final List<PieceListener> listeners = getCopyOfListeners();
    environment.getExecutor().execute(new Runnable() {
      public void run() {
        try {
          for (PieceListener pieceListener : listeners) {
            pieceListener.gotPiece(pieceIndex);
          }
        } catch (Exception e) {
          logger.error("exception during notification of piece listeners", e);
        }
      }
    });
  }

  private synchronized List<PieceListener> getCopyOfListeners() {
    return new ArrayList<PieceListener>(listeners);
  }

  public synchronized void addPieceListener(PieceListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  public synchronized void removePieceListener(PieceListener listener) {
    listeners.remove(listener);
  }
}
