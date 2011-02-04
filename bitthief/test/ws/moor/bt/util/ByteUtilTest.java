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

package ws.moor.bt.util;

import java.util.BitSet;

/**
 * TODO(pmoor): Javadoc
 */
public class ByteUtilTest extends ExtendedTestCase {

  byte[] buffer32 = new byte[4];
  byte[] buffer64 = new byte[8];

  public void testIntToB32() {
    ByteUtil.int_to_b32(42, buffer32);
    assertArrayEquals(ByteUtil.newByteArray(0, 0, 0, 42), buffer32);
  }

  public void testIntToL32() {
    ByteUtil.int_to_l32(42, buffer32);
    assertArrayEquals(ByteUtil.newByteArray(42, 0, 0, 0), buffer32);
  }

  public void testIntToB32withOffset() {
    ByteUtil.int_to_b32(728, buffer64, 3);
    assertArrayEquals(ByteUtil.newByteArray(0, 0, 0, 0, 0, 2, 216, 0), buffer64);
  }

  public void testNewByteArray() {
    assertArrayEquals(new byte[] {1, 2, 3, 4, 5}, ByteUtil.newByteArray(1, 2, 3, 4, 5));
    assertArrayEquals(new byte[] {5, 4, 1, 3, 2}, ByteUtil.newByteArray(5, 4, 1, 3, 2));
    assertArrayEquals(new byte[] {1}, ByteUtil.newByteArray(1));
    assertArrayEquals(new byte[] {}, ByteUtil.newByteArray());
    assertArrayEquals(new byte[] {77, 42}, ByteUtil.newByteArray(77, 42));
    assertArrayEquals(new byte[] {(byte) 250, (byte) 240}, ByteUtil.newByteArray(250, 240));
  }

  public void testNewByteArrayFailing() {
    try {
      ByteUtil.newByteArray(300);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testB32ToInt() {
    assertEquals(0, ByteUtil.b32_to_int(ByteUtil.newByteArray(0, 0, 0, 0), 0));
    assertEquals(42, ByteUtil.b32_to_int(ByteUtil.newByteArray(0, 0, 0, 42), 0));
    assertEquals(17, ByteUtil.b32_to_int(ByteUtil.newByteArray(0, 0, 0, 17), 0));
    assertEquals(300, ByteUtil.b32_to_int(ByteUtil.newByteArray(0, 0, 1, 44), 0));
    assertEquals(0xffffffff, ByteUtil.b32_to_int(ByteUtil.newByteArray(255, 255, 255, 255), 0));
    assertEquals((1 << 24) + 42, ByteUtil.b32_to_int(ByteUtil.newByteArray(1, 0, 0, 42), 0));
    assertEquals((18 << 16) | (22 << 8), ByteUtil.b32_to_int(ByteUtil.newByteArray(38, 0, 18, 22, 0, 22, 15), 1));
  }

  public void testRandomByteArray() {
    assertEquals(0, ByteUtil.randomByteArray(0).length);
    assertEquals(5, ByteUtil.randomByteArray(5).length);
    assertEquals(16, ByteUtil.randomByteArray(16).length);
  }

  public void testXORDistance() {
    assertEquals(199, ByteUtil.distance(
        ByteUtil.newByteArray(0, 0, 0, 0), ByteUtil.newByteArray(1, 1, 1, 1)));
    assertEquals(35, ByteUtil.distance(
        ByteUtil.newByteArray(64, 32, 128, 16), ByteUtil.newByteArray(32, 64, 64, 64)));
    assertEquals(800, ByteUtil.distance(
        ByteUtil.newByteArray(18), ByteUtil.newByteArray(18)));
    assertEquals(0, ByteUtil.distance(
        ByteUtil.newByteArray(), ByteUtil.newByteArray()));
    assertEquals(0, ByteUtil.distance(
        ByteUtil.newByteArray(0, 0), ByteUtil.newByteArray(255, 255)));
    byte[] rnd = ByteUtil.randomByteArray(20);
    assertEquals(800, ByteUtil.distance(rnd, rnd));
  }

  public void testToBitSet() {
    byte[] array = ByteUtil.newByteArray(0x15, 0x88, 0x22, 0x10);
    BitSet set = new BitSet(4 * 8);
    set.set(3);
    set.set(5);
    set.set(7);
    set.set(8);
    set.set(12);
    set.set(18);
    set.set(22);
    set.set(27);

    assertEquals(set, ByteUtil.toBitSet(array));
  }

  public void testXOR() {
    byte[] a = ByteUtil.newByteArray(0, 0, 0, 0);
    byte[] b = ByteUtil.randomByteArray(4);
    assertArrayEquals(b, ByteUtil.xor(a, b));
    assertArrayEquals(b, ByteUtil.xor(b, a));
    assertArrayEquals(a, ByteUtil.xor(a, a));
    assertArrayEquals(a, ByteUtil.xor(b, b));
  }

  public void testGetLeftmostBit() {
    byte[] a = ByteUtil.newByteArray(0x00, 0x07, 0x33);
    assertEquals(10, ByteUtil.leftmostBit(a));
    byte[] b = ByteUtil.newByteArray(0x00, 0x00, 0x33);
    assertEquals(5, ByteUtil.leftmostBit(b));
    byte[] c = ByteUtil.newByteArray(0xf0, 0x00, 0x33);
    assertEquals(23, ByteUtil.leftmostBit(c));
    byte[] d = ByteUtil.newByteArray(0x00, 0x00, 0x00);
    assertEquals(-1, ByteUtil.leftmostBit(d));
  }

  public void testURLEncode() {
    byte[] bytes = ByteUtil.newByteArray(0x82, 0xff, 0x13, 0x07);
    assertEquals("%82%ff%13%07", ByteUtil.urlEncode(bytes));

    bytes = ByteUtil.newByteArray('a', 'b', 0xff, 0x02, 0x09);
    assertEquals("ab%ff%02%09", ByteUtil.urlEncode(bytes));
    
    bytes = ByteUtil.newByteArray('M', '-', '0', '9', 'z');
    assertEquals("M-09z", ByteUtil.urlEncode(bytes));
  }
}
