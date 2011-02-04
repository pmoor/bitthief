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

import ws.moor.bt.util.ExtendedTestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * TODO(pmoor): Javadoc
 */
public class BitFieldTest extends ExtendedTestCase {

  private BitField bitField;
  private static final int PIECE_COUNT = 42;

  protected void setUp() throws Exception {
    super.setUp();
    bitField = new BitField(PIECE_COUNT);
  }

  public void testNewOne() {
    for (int i = 0; i < PIECE_COUNT; i++) {
      assertFalse(bitField.hasPiece(i));
    }
  }

  public void testVarargConstructor() {
    BitField expected = new BitField(16);
    expected.gotPiece(2);
    expected.gotPiece(7);
    expected.gotPiece(3);
    assertEquals(expected, new BitField(16, 3, 7, 2));
  }

  public void testSomeSettingAndUnsetting() {
    fillSparse();
    assertSparse();
  }

  private void assertSparse() {
    for (int i = 0; i < PIECE_COUNT; i++) {
      if (i % 3 == 2 || i % 7 == 3) {
        assertTrue(bitField.hasPiece(i));
      } else {
        assertFalse(bitField.hasPiece(i));
      }
    }
  }

  private void fillSparse() {
    for (int i = 0; i < PIECE_COUNT; i++) {
      if (i % 3 == 2 || i % 7 == 3) {
        bitField.gotPiece(i);
      }
    }
  }

  public void testPieceIndexTooLargeForHas() {
    try {
      bitField.hasPiece(PIECE_COUNT);
      fail("should fail");
    } catch (Exception e) {
      // expected
    }
  }

  public void testPieceIndexTooLargeForGot() {
    try {
      bitField.gotPiece(PIECE_COUNT);
      fail("should fail");
    } catch (Exception e) {
      // expected
    }
  }

  public void testPieceIndexTooSmallForHas() {
    try {
      bitField.hasPiece(-1);
      fail("should fail");
    } catch (Exception e) {
      // expected
    }
  }

  public void testPieceIndexTooSmallForGot() {
    try {
      bitField.gotPiece(-1);
      fail("should fail");
    } catch (Exception e) {
      // expected
    }
  }

  public void testToArray() {
    fillSparse();
    byte[] expected = new byte[PIECE_COUNT / 8 + 1];
    for (int i = 0; i < PIECE_COUNT; i++) {
      if (i % 3 == 2 || i % 7 == 3) {
        int byteIndex = i / 8;
        int bitIndex = 7 - i % 8;
        expected[byteIndex] |= 1 << bitIndex;
      }
    }
    assertArrayEquals(expected, bitField.toArray());
  }

  public void testToArrayFits() {
    bitField = new BitField(8);
    assertArrayEquals(new byte[1], bitField.toArray());
  }

  public void testToString() {
    fillSparse();
    assertEquals("18/42: {2, 3, 5, 8, 10, 11, 14, 17, 20, 23, 24, 26, 29, 31, 32, 35, 38, 41}",
        bitField.toString());
  }

  public void testEqualsAndHashCode() {
    BitField bitFieldA = new BitField(8);
    BitField bitFieldB = new BitField(8);
    assertEquals(bitFieldA, bitFieldB);
    assertEquals(bitFieldA.hashCode(), bitFieldB.hashCode());

    bitFieldA.gotPiece(7);
    bitFieldB.gotPiece(7);

    assertEquals(bitFieldA, bitFieldB);
    assertEquals(bitFieldA.hashCode(), bitFieldB.hashCode());
  }

  public void testClone() {
    fillSparse();
    BitField bitFieldB = bitField.clone();
    assertEquals(bitField, bitFieldB);
    assertNotSame(bitField, bitFieldB);
  }

  public void testByteCount() {
    assertEquals(6, bitField.getByteCount());
    assertEquals(0, new BitField(0).getByteCount());
    assertEquals(1, new BitField(1).getByteCount());
    assertEquals(1, new BitField(8).getByteCount());
    assertEquals(2, new BitField(9).getByteCount());
    assertEquals(50, new BitField(400).getByteCount());
  }

  public void testFromArray() {
    BitField bitFieldCopy = BitField.fromArray(bitField.toArray(), 0, 6);
    assertArrayEquals(bitField.toArray(), bitFieldCopy.toArray());
    assertNotEquals(bitField, bitFieldCopy);
    assertFalse(bitFieldCopy.hasPiece(47));
  }

  public void testDowncastOneByte() {
    BitField a = new BitField(8);
    try {
      a.downcastTo(0);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      a.downcastTo(9);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
    for (int i = 8; i > 0; i--) {
      a.downcastTo(i);
      assertEquals(1, a.getByteCount());
    }
  }

  public void testDowncastMultipleBytes() {
    BitField a = new BitField(12);
    try {
      a.downcastTo(8);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }

    for (int i = 12; i > 8; i--) {
      a.downcastTo(i);
      assertEquals(2, a.getByteCount());
    }
  }

  public void testGetPieceCount() {
    int tests[] = new int[] {7, 0, 3, 4, 88};
    for (int count : tests) {
      assertEquals(count, new BitField(count).getPieceCount());
    }
  }

  public void testMinus() {
    BitField a = new BitField(8, 1, 2, 3, 5, 7);
    BitField b = new BitField(8, 0, 1, 2, 3, 4);
    BitField c = new BitField(8, 5, 7);
    assertEquals(c, a.minus(b));
  }

  public void testAvailablePieces() {
    BitField a = new BitField(32, 7, 14, 12, 18, 25);
    List<Integer> expected = Arrays.asList(7, 12, 14, 18, 25);
    assertEquals(expected, a.availablePieces());
  }

  public void testGetAvailablePieceCount() {
    BitField a = new BitField(32, 7, 14, 12, 18, 25);
    assertEquals(5, a.getAvailablePieceCount());
    a.gotPiece(27);
    assertEquals(6, a.getAvailablePieceCount());
    a.gotPiece(27);
    assertEquals(6, a.getAvailablePieceCount());
  }

  public void testGetMissingPieceCount() {
    assertEquals(32, new BitField(32).getMissingPieceCount());
    assertEquals(16, new BitField(16).getMissingPieceCount());
    assertEquals(15, new BitField(16, 7).getMissingPieceCount());
    assertEquals(14, new BitField(16, 7, 3).getMissingPieceCount());
    assertEquals(18, new BitField(20, 7, 3, 3).getMissingPieceCount());
    assertEquals(120, new BitField(128, 7, 3, 4, 99, 9, 1, 2, 127).getMissingPieceCount());
  }

  public void testHasAll() {
    assertTrue(new BitField(4, 0, 1, 2, 3).hasAll());
    assertTrue(new BitField(0).hasAll());
    assertTrue(new BitField(1, 0).hasAll());
    assertFalse(new BitField(3, 0, 1).hasAll());
    assertFalse(new BitField(100, 7).hasAll());
  }

  public void testSetRandomPieces() {
    BitField a = new BitField(20);
    a.setRandomPieces(5, rnd);
    assertEquals(5, a.getAvailablePieceCount());
  }

  public void testSetRandomPiecesWithSameSeed() {
    for (int i = 0; i < 10; i++) {
      int size = rnd.nextInt(50) + 5;
      BitField a = new BitField(size);
      BitField b = new BitField(size);
      int setting = rnd.nextInt(size);
      long seed = rnd.nextLong();
      a.setRandomPieces(setting, new Random(seed));
      b.setRandomPieces(setting, new Random(seed));
      assertEquals(a, b);
      assertEquals(setting, a.getAvailablePieceCount());
    }
  }

  public void testSetRandomPiecesEdgeCases() {
    BitField a = new BitField(20);
    a.setRandomPieces(0, rnd);
    assertEquals(0, a.getAvailablePieceCount());
    a.setRandomPieces(20, rnd);
    assertEquals(20, a.getAvailablePieceCount());
    try {
      a.setRandomPieces(21, rnd);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      a.setRandomPieces(-1, rnd);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
