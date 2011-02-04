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

import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class BlockTest extends ExtendedTestCase {

  public void testEquals() {
    assertEqualsForParameters(7, 3, 2);
    assertEqualsForParameters(1, 20, 15);
    assertEqualsForParameters(900, 128, 50000);
    assertEqualsForParameters(12, 0, 16384);
  }

  private void assertEqualsForParameters(int pieceIndex, int blockOffset, int blockLength) {
    Block blockA = new Block(pieceIndex, blockOffset, blockLength);
    Block blockB = new Block(pieceIndex, blockOffset, blockLength);
    assertEquals(blockA, blockB);
    assertEquals(blockA.hashCode(), blockB.hashCode());
    assertEquals(pieceIndex, blockA.getPieceIndex());
    assertEquals(blockOffset, blockA.getOffset());
    assertEquals(blockLength, blockA.getLength());
    assertEquals(blockOffset + blockLength, blockA.getEnd());
  }

  public void testToString() {
    assertEquals("18:0-9000", new Block(18, 0, 9000).toString());
  }

  public void testCompare() {
    Block blockA = new Block(5, 10, 5);
    Block blockB = new Block(10, 5, 4);
    Block blockC = new Block(10, 6, 3);
    assertTrue(blockA.compareTo(blockA) == 0);
    assertTrue(blockA.compareTo(blockB) < 0);
    assertTrue(blockA.compareTo(blockC) < 0);
    assertTrue(blockB.compareTo(blockA) > 0);
    assertTrue(blockB.compareTo(blockB) == 0);
    assertTrue(blockB.compareTo(blockC) < 0);
    assertTrue(blockC.compareTo(blockA) > 0);
    assertTrue(blockC.compareTo(blockB) > 0);
    assertTrue(blockC.compareTo(blockC) == 0);
  }

  public void testCopyConstructor() {
    Block blockA = new Block(5, 10, 5);
    Block blockB = new Block(blockA);
    assertEquals(blockA, blockB);
    assertNotSame(blockA, blockB);
  }
}
