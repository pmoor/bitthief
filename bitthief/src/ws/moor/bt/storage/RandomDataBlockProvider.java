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
import java.util.List;
import java.util.Random;

/**
 * TODO(pmoor): Javadoc
 */
public class RandomDataBlockProvider implements DataBlockProvider {

  private final int pieceCount;
  private final int pieceLength;
  private final int lastPieceLength;
  private final int blocksToRefuse;

  private static final Random rnd = new Random();
  private static final int MAX_BLOCK_LENGTH = 32 * 1024;
  private static final int USUAL_BLOCK_LENGTH = 16 * 1024;
  private static final int DEFAULT_BLOCKS_TO_REFUSE = 1;

  private static final int aRandomInteger = rnd.nextInt();

  public RandomDataBlockProvider(int pieceCount, int pieceLength, int lastPieceLength) {
    this(pieceCount, pieceLength, lastPieceLength, DEFAULT_BLOCKS_TO_REFUSE);
  }

  public RandomDataBlockProvider(int pieceCount, int pieceLength, int lastPieceLength, int blocksToRefuse) {
    this.pieceCount = pieceCount;
    this.pieceLength = pieceLength;
    this.lastPieceLength = lastPieceLength;
    this.blocksToRefuse = blocksToRefuse;
  }

  public DataBlock getBlock(Block block) throws BlockRefusedException {
    assertValidBlockBounds(block);
    assertBlockIWantToProvide(block);
    byte[] data = new byte[block.getLength()];
    rnd.nextBytes(data);
    return new DataBlock(block.getPieceIndex(), block.getOffset(), data);
  }

  private void assertValidBlockBounds(Block block) {
    int length = getPieceLength(block);
    if (block.getPieceIndex() < 0 || block.getPieceIndex() >= pieceCount) {
      throw new IllegalArgumentException("invalid piece index: " + block);
    } else if (block.getOffset() < 0 || block.getOffset() >= length) {
      throw new IllegalArgumentException("invalid offset: " + block);
    } else if (block.getLength() < 0 || block.getLength() > MAX_BLOCK_LENGTH) {
      throw new IllegalArgumentException("invalid length: " + block);
    } else if (block.getOffset() + block.getLength() > length) {
      throw new IllegalArgumentException("invalid length: " + block);
    }
  }

  private int getPieceLength(Block block) {
    return (block.getPieceIndex() == pieceCount - DEFAULT_BLOCKS_TO_REFUSE) ? lastPieceLength : pieceLength;
  }

  private void assertBlockIWantToProvide(Block block) throws BlockRefusedException {
    if (denyBlockTransfer(block)) {
      throw new BlockRefusedException("not allowed to transfer this block");
    }
  }

  private boolean denyBlockTransfer(Block block) {
    int[] forbiddenBytes = getForbiddenBytes(block);
    for (int forbiddenByte : forbiddenBytes) {
      if (block.getOffset() <= forbiddenByte && block.getOffset() + block.getLength() > forbiddenByte) {
        return true;
      }
    }
    return false;
  }

  private int[] getForbiddenBytes(Block block) {
    int pieceLength = getPieceLength(block);
    int blockCount = pieceLength / USUAL_BLOCK_LENGTH;
    List<Integer> blockOffsets = new ArrayList<Integer>(blockCount);
    for (int i = 0; i < blockCount; i++) {
      blockOffsets.add(i * USUAL_BLOCK_LENGTH);
    }
    Collections.shuffle(blockOffsets, new Random(block.getPieceIndex() * aRandomInteger));
    int[] forbiddenBytes = new int[blocksToRefuse];
    for (int i = 0; i < blocksToRefuse; i++) {
      if (blockOffsets.size() <= i) {
        forbiddenBytes[i] = 7;
      } else {
        forbiddenBytes[i] = blockOffsets.get(i) + USUAL_BLOCK_LENGTH / 2;
      }
    }
    return forbiddenBytes;
  }
}
