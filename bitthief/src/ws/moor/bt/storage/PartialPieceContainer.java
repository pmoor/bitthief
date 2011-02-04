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

import ws.moor.bt.downloader.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO(pmoor): Javadoc
 */
public class PartialPieceContainer {

  private final Map<Integer, List<DataBlock>> incompletePieces;

  public PartialPieceContainer() {
    incompletePieces = new HashMap<Integer, List<DataBlock>>();
  }

  public synchronized void addBlock(DataBlock block) {
    List<DataBlock> existingBlocks = incompletePieces.get(block.getPieceIndex());
    if (existingBlocks == null) {
      existingBlocks = new ArrayList<DataBlock>();
      incompletePieces.put(block.getPieceIndex(), existingBlocks);
    }
    existingBlocks.add(block);
  }

  public synchronized byte[] checkPieceForCompleteness(int pieceIndex, int pieceLength) {
    List<DataBlock> existingBlocks = incompletePieces.get(pieceIndex);
    if (existingBlocks == null) {
      return null;
    }
    Collections.sort(existingBlocks);
    byte[] piece = new byte[pieceLength];
    int offset = 0;
    for (DataBlock block : existingBlocks) {
      if (block.getOffset() > offset) {
        return null;
      }
      int writing = block.getLength() - (offset - block.getOffset());
      writing = Math.min(writing, pieceLength - offset);
      System.arraycopy(block.getData(), offset - block.getOffset(), piece, offset, writing);
      offset += writing;
      if (offset >= pieceLength) {
        break;
      }
    }

    if (offset < pieceLength) {
      return null;
    }

    // got a complete piece
    incompletePieces.remove(pieceIndex);
    return piece;
  }

  public synchronized List<Block> getMissingBlocks(int pieceIndex, int pieceLength, int maxBlockLength) {
    List<? extends Block> existingBlocks = incompletePieces.get(pieceIndex);
    if (existingBlocks == null) {
      existingBlocks = new ArrayList<DataBlock>(0);
    }
    Collections.sort(existingBlocks);
    int offset = 0;
    List<Block> result = new ArrayList<Block>(pieceLength / maxBlockLength);
    for (Block block : existingBlocks) {
      if (block.getOffset() > offset) {
        for (int start = offset; start < block.getOffset(); start += maxBlockLength) {
          result.add(new Block(
              pieceIndex,
              start,
              Math.min(maxBlockLength, block.getOffset() - start)));
        }
      }
      offset = block.getEnd();
    }

    if (offset < pieceLength) {
      for (int start = offset; start < pieceLength; start += maxBlockLength) {
        result.add(new Block(
            pieceIndex,
            start,
            Math.min(maxBlockLength, pieceLength - start)));
      }
    }

    return result;
  }

  public synchronized List<Integer> getPartiallyAvailablePieces() {
    return new ArrayList<Integer>(incompletePieces.keySet());
  }

  public int getPartiallyAvailablePiecesCount() {
    return incompletePieces.size();
  }

}
