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

package ws.moor.bt.downloader;

import org.apache.log4j.Logger;
import ws.moor.bt.storage.BitField;
import ws.moor.bt.storage.PieceManager;
import ws.moor.bt.torrent.MetaInfo;
import ws.moor.bt.util.LoggingUtil;
import ws.moor.bt.util.TimeoutSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class DummyNextBlockStrategy implements NextBlockStrategy {

  private final MetaInfo metaInfo;
  private final PieceManager pieceManager;

  private final TimeoutSet<Block> recentBlocks = new TimeoutSet<Block>(4096, TIMEOUT);

  private static final int TIMEOUT = 16 * 60 * 1000;

  private static final int BLOCK_LENGTH = 16384;

  private static final Logger logger = LoggingUtil.getLogger(DummyNextBlockStrategy.class);

  public DummyNextBlockStrategy(MetaInfo metaInfo, PieceManager pieceManager) {
    this.metaInfo = metaInfo;
    this.pieceManager = pieceManager;
  }

  public synchronized List<Block> getMoreBlocks(BitField available, int numberOfBlocks) {
    List<Block> result = new ArrayList<Block>();
    if (numberOfBlocks < 1) {
      return result;
    }

    BitField important = available.minus(pieceManager.getValidPieces());
    List<Integer> missingPieces = important.availablePieces();
    if (missingPieces.isEmpty()) {
      logger.info("no missing pieces the peer could offer me");
      return result;
    }

    adaptRecentBlocksSet();

    List<Integer> partiallyAvailablePieces = pieceManager.getPartiallyAvailablePieces();
    partiallyAvailablePieces.retainAll(missingPieces);
    Collections.sort(partiallyAvailablePieces); // sort the pieces to finish the first ones more quick

    missingPieces.removeAll(partiallyAvailablePieces);
    Collections.shuffle(missingPieces);

    partiallyAvailablePieces.addAll(missingPieces);
    extractBlocksForPieces(partiallyAvailablePieces, numberOfBlocks, result, false);

    checkResultLargeEnough(result, numberOfBlocks, partiallyAvailablePieces);
    return result;
  }

  private void adaptRecentBlocksSet() {
    int overallMissingPieces = pieceManager.getValidPieces().getMissingPieceCount();
    int missingBlocks = metaInfo.getPieceLength() * overallMissingPieces / BLOCK_LENGTH;
    int category = missingBlocks / 512;
    category = Math.max(1, Math.min(8, category));
    int newSize = category * 512;
    if (recentBlocks.size() != newSize) {
      logger.info("resizing recent block set to " + newSize + " entries");
      recentBlocks.resize(newSize);
    }
    recentBlocks.setTimeout(TIMEOUT * category / 8);
  }

  private void checkResultLargeEnough(List<Block> result, int numberOfBlocks, List<Integer> pieces) {
    if (result.size() < numberOfBlocks) {
      logger.debug("was not able to fulfill request for " + numberOfBlocks +
          " blocks, returning a random, possible duplicate set of blocks now out of " + pieces);
      Collections.shuffle(pieces);
      extractBlocksForPieces(pieces, numberOfBlocks, result, true);
    }
  }

  private void extractBlocksForPieces(List<Integer> pieces, int numberOfBlocksToExtract,
                                      List<Block> result, boolean duplicates) {
    for (int piece : pieces) {
      for (Block block : pieceManager.getMissingBlocks(piece, BLOCK_LENGTH)) {
        addBlockIfPermitted(block, result, duplicates);
        if (result.size() == numberOfBlocksToExtract) {
          return;
        }
      }
    }
  }

  private void addBlockIfPermitted(Block block, List<Block> result, boolean duplicates) {
    if (!recentBlocks.contains(block) || duplicates) {
      recentBlocks.add(block);
      result.add(block);
    }
  }

  public synchronized void returnBlocks(Collection<Block> blocks) {
    for (Block block : blocks) {
      recentBlocks.remove(block);
    }
  }
}
