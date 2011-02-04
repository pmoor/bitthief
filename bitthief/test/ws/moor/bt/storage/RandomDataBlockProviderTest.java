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
import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class RandomDataBlockProviderTest extends ExtendedTestCase {

  private RandomDataBlockProvider provider;

  private static final int BLOCK_LENGTH = 16384;
  private static final int PIECE_COUNT = 4096;
  private static final int PIECE_LENGTH = 128 * 1024;
  private static final int LAST_PIECE_LENGTH = 32 * 1024;

  public void testCases() {
    for (int i = 0; i < 100; i++) {
      int pieceIndex = rnd.nextInt(Integer.MAX_VALUE) % (PIECE_COUNT - 1);
      int refused = 0;
      for (int offset = 0; offset < PIECE_LENGTH; offset += BLOCK_LENGTH) {
        try {
          testCase(pieceIndex, offset, BLOCK_LENGTH);
        } catch (DataBlockProvider.BlockRefusedException e) {
          refused++;
        }
      }
      assertEquals(1, refused);
    }
  }

  public void testLastPiece() {
    for (int i = 0; i < 100; i++) {
      int pieceIndex = PIECE_COUNT - 1;
      int refused = 0;
      for (int offset = 0; offset < LAST_PIECE_LENGTH; offset += BLOCK_LENGTH) {
        try {
          testCase(pieceIndex, offset, BLOCK_LENGTH);
        } catch (DataBlockProvider.BlockRefusedException e) {
          refused++;
        }
      }
      assertEquals(1, refused);
    }
  }

  public void testSomeIllegalOffsets() throws DataBlockProvider.BlockRefusedException {
    illegalTestCase(0, -1, 5);
    illegalTestCase(0, -1, -5);
    illegalTestCase(0, 1, -5);
    illegalTestCase(0, PIECE_LENGTH, 58);
    illegalTestCase(0, PIECE_LENGTH, -5000);
    illegalTestCase(0, PIECE_LENGTH - 50, 55);
    illegalTestCase(-1, 50, 200);
    illegalTestCase(PIECE_COUNT, 0, 10);
    illegalTestCase(PIECE_COUNT - 1, LAST_PIECE_LENGTH, 0);
    illegalTestCase(PIECE_COUNT - 1, LAST_PIECE_LENGTH - 10, 15);
    illegalTestCase(PIECE_COUNT - 1, 0, LAST_PIECE_LENGTH + 1);
  }

  public void testMultipleForbiddenBlocks() {
    for (int i = 0; i < 100; i++) {
      int forbiddenBlocks = rnd.nextInt(PIECE_LENGTH / BLOCK_LENGTH + 1);
      provider = new RandomDataBlockProvider(PIECE_COUNT, PIECE_LENGTH, LAST_PIECE_LENGTH, forbiddenBlocks);
      int pieceIndex = rnd.nextInt(Integer.MAX_VALUE) % (PIECE_COUNT - 1);
      int refused = 0;
      for (int offset = 0; offset < PIECE_LENGTH; offset += BLOCK_LENGTH) {
        try {
          testCase(pieceIndex, offset, BLOCK_LENGTH);
        } catch (DataBlockProvider.BlockRefusedException e) {
          refused++;
        }
      }
      assertEquals(forbiddenBlocks, refused);
    }
  }

  public void testMultipleForbiddenBlocksInLastPiece() {
    for (int i = 0; i < 100; i++) {
      int forbiddenBlocks = rnd.nextInt(LAST_PIECE_LENGTH / BLOCK_LENGTH + 1);
      provider = new RandomDataBlockProvider(PIECE_COUNT, PIECE_LENGTH, LAST_PIECE_LENGTH, forbiddenBlocks);
      int pieceIndex = PIECE_COUNT - 1;
      int refused = 0;
      for (int offset = 0; offset < LAST_PIECE_LENGTH; offset += BLOCK_LENGTH) {
        try {
          testCase(pieceIndex, offset, BLOCK_LENGTH);
        } catch (DataBlockProvider.BlockRefusedException e) {
          refused++;
        }
      }
      assertEquals(forbiddenBlocks, refused);
    }
  }

  private void illegalTestCase(int pieceIndex, int offset, int length)
      throws DataBlockProvider.BlockRefusedException {
    try {
      testCase(pieceIndex, offset, length);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  private void testCase(int pieceIndex, int offset, int length)
      throws DataBlockProvider.BlockRefusedException {
    Block block = new Block(pieceIndex, offset, length);
    DataBlock dataBlock = provider.getBlock(block);
    assertEquals(pieceIndex, dataBlock.getPieceIndex());
    assertEquals(offset, dataBlock.getOffset());
    assertEquals(length, dataBlock.getLength());
    assertEquals(length, dataBlock.getData().length);
  }

  protected void setUp() throws Exception {
    super.setUp();
    provider = new RandomDataBlockProvider(PIECE_COUNT, PIECE_LENGTH, LAST_PIECE_LENGTH);
  }
}
