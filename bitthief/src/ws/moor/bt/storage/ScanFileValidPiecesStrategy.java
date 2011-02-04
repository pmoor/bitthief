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
import ws.moor.bt.torrent.MetaInfo;
import ws.moor.bt.util.LoggingUtil;


/**
 * TODO(pmoor): Javadoc
 */
public class ScanFileValidPiecesStrategy implements ValidPiecesStrategy {

  private BitField validPieces;

  private final Storage storage;
  private final MetaInfo metaInfo;

  private static final Logger logger = LoggingUtil.getLogger(ScanFileValidPiecesStrategy.class);

  public ScanFileValidPiecesStrategy(Storage storage, MetaInfo metaInfo) {
    this.storage = storage;
    this.metaInfo = metaInfo;
    verifyPieces();
  }

  private void verifyPieces() {
    logger.info("starting piece verification");
    validPieces = new BitField(storage.getPieceCount());

    byte[] pieceBuffer = new byte[storage.getPieceLength()];
    int pieceCount = storage.getPieceCount();
    for (int pieceIndex = 0; pieceIndex < pieceCount; pieceIndex++) {
      if (pieceIndex % 64 == 0) {
        logger.debug("verifying piece " + pieceIndex + "/" + pieceCount);
      }
      int read = storage.readPiece(pieceIndex, pieceBuffer);
      if (metaInfo.getPieceHashes()[pieceIndex].equalsHashOf(pieceBuffer, 0, read)) {
        validPieces.gotPiece(pieceIndex);
      }
    }
    logger.info("successfully verified pieces");
    logger.info("got " + validPieces.getAvailablePieceCount() + " out of " + validPieces.getPieceCount());
    logger.debug("available pieces: " + validPieces);
  }

  public BitField getValidPieces() {
    return validPieces.clone();
  }

  public boolean hasPiece(int pieceIndex) {
    return validPieces.hasPiece(pieceIndex);
  }

  public void gotPiece(int pieceIndex) {
    validPieces.gotPiece(pieceIndex);
  }
}
