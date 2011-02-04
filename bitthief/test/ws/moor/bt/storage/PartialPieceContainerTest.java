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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class PartialPieceContainerTest extends ExtendedTestCase {

  private PartialPieceContainer container;

  private static final int PIECE_LENGTH = 128;
  private static final int LAST_PIECE_LENGTH = 64;

  protected void setUp() throws Exception {
    super.setUp();
    container = new PartialPieceContainer();
  }

  public void testGetMissingBlocks() {
    container.addBlock(new DataBlock(0, 60, new byte[32]));

    List<Block> blocks = container.getMissingBlocks(0, PIECE_LENGTH, 30);
    assertEquals(4, blocks.size());
    assertEquals(new Block(0, 0, 30), blocks.get(0));
    assertEquals(new Block(0, 30, 30), blocks.get(1));
    assertEquals(new Block(0, 92, 30), blocks.get(2));
    assertEquals(new Block(0, 122, 6), blocks.get(3));
  }

  public void testGetMissingBlocksWhole() {
    List<Block> blocks = container.getMissingBlocks(5, PIECE_LENGTH, 32);
    assertEquals(4, blocks.size());
    assertEquals(new Block(5,  0, 32), blocks.get(0));
    assertEquals(new Block(5, 32, 32), blocks.get(1));
    assertEquals(new Block(5, 64, 32), blocks.get(2));
    assertEquals(new Block(5, 96, 32), blocks.get(3));
  }

  public void testGetMissingBlocksLastPiece() {
    container.addBlock(new DataBlock(7, 30, new byte[15]));
    List<Block> blocks = container.getMissingBlocks(7, LAST_PIECE_LENGTH, 16);

    assertEquals(4, blocks.size());
    assertEquals(new Block(7,  0, 16), blocks.get(0));
    assertEquals(new Block(7, 16, 14), blocks.get(1));
    assertEquals(new Block(7, 45, 16), blocks.get(2));
    assertEquals(new Block(7, 61,  3), blocks.get(3));
  }

  public void testPartiallyAvailablePieces() {
    container.addBlock(new DataBlock(5, 30, new byte[15]));
    container.addBlock(new DataBlock(10, 30, new byte[15]));
    container.addBlock(new DataBlock(11, 30, new byte[15]));

    List<Integer> partiallyAvailable = container.getPartiallyAvailablePieces();
    Collections.sort(partiallyAvailable);
    assertEquals(Arrays.asList(5, 10, 11), partiallyAvailable);
  }

  public void testPartiallyAvailablePiecesEmpty() {
    List<Integer> partiallyAvailable = container.getPartiallyAvailablePieces();
    assertTrue(partiallyAvailable.isEmpty());
  }

  public void testGetPartiallyAvailablePiecesCount() {
    assertEquals(0, container.getPartiallyAvailablePiecesCount());
    container.addBlock(new DataBlock(5, 30, new byte[15]));
    assertEquals(1, container.getPartiallyAvailablePiecesCount());
    container.addBlock(new DataBlock(10, 30, new byte[15]));
    assertEquals(2, container.getPartiallyAvailablePiecesCount());
    container.addBlock(new DataBlock(11, 30, new byte[15]));
    assertEquals(3, container.getPartiallyAvailablePiecesCount());
    container.addBlock(new DataBlock(11, 0, new byte[30]));
    assertEquals(3, container.getPartiallyAvailablePiecesCount());

    container.checkPieceForCompleteness(11, 45);
    assertEquals(2, container.getPartiallyAvailablePiecesCount());
  }

  public void testWithTooLargeLastBlock() {
    assertEquals(0, container.getPartiallyAvailablePiecesCount());
    container.addBlock(new DataBlock(0, 0, new byte[32]));
    container.addBlock(new DataBlock(0, 32, new byte[32]));
    container.addBlock(new DataBlock(0, 96, new byte[34])); // <-- too long

    byte[] result = container.checkPieceForCompleteness(0, 128);
    assertNull(result);
    
    container.addBlock(new DataBlock(0, 64, new byte[32]));
    result = container.checkPieceForCompleteness(0, 128);
    assertNotNull(result);
  }

  public void testAddingTheSameBlockTwice() {
    assertEquals(0, container.getPartiallyAvailablePiecesCount());
    container.addBlock(new DataBlock(0, 0, new byte[32]));
    container.addBlock(new DataBlock(0, 32, new byte[32]));
    container.addBlock(new DataBlock(0, 32, new byte[32])); // <-- second time
    container.addBlock(new DataBlock(0, 64, new byte[64]));

    byte[] result = container.checkPieceForCompleteness(0, 128);
    assertNotNull(result);
  }
}
