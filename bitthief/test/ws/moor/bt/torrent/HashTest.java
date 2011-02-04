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

package ws.moor.bt.torrent;

import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.DigestUtil;
import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class HashTest extends ExtendedTestCase {

  private byte[] exampleHashA;
  private byte[] exampleHashB;

  protected void setUp() throws Exception {
    super.setUp();
    exampleHashA = new byte[20];
    exampleHashB = new byte[20];
    for (byte i = 0; i < 20; i++) {
      exampleHashA[i] = i;
      exampleHashB[i] = (byte) (20 - i);
    }
  }

  public void testCreation() {
    Hash infoHash = new Hash(exampleHashA);
    assertArrayEquals(exampleHashA, infoHash.getBytes());
  }

  public void testInvalidCreation() {
    try {
      new Hash(new byte[0]);
      fail("should fail");
    } catch (Exception e) {
      // expected
    }
  }

  public void testLongCreation() {
    try {
      new Hash(new byte[21]);
      fail("should fail");
    } catch (Exception e) {
      // expected
    }
  }

  public void testNullCreation() {
    try {
      new Hash(null);
      fail("should fail");
    } catch (Exception e) {
      // expected
    }
  }

  public void testEquals() {
    Hash a = new Hash(exampleHashA);
    Hash b = new Hash(exampleHashA);
    assertEquals(a, b);
  }

  public void testNotEquals() {
    Hash a = new Hash(exampleHashA);
    Hash b = new Hash(exampleHashB);
    assertFalse(a.equals(b));
  }

  public void testHash() {
    Hash a = new Hash(exampleHashA);
    Hash b = new Hash(exampleHashA);
    assertEquals(a.hashCode(), b.hashCode());
  }

  public void testForByteArray() {
    byte[] message = "Hello World".getBytes();
    Hash a = Hash.forByteArray(message);
    Hash b = new Hash(DigestUtil.sha1(message));
    assertEquals(a, b);
  }

  public void testForByteArrayWithOffset() {
    byte[] message = "Hello World".getBytes();
    Hash a = Hash.forByteArray(message, 3, 4);
    Hash b = new Hash(DigestUtil.sha1(message, 3, 4));
    assertEquals(a, b);
  }

  public void testFromConcatenation() {
    byte[] array = new byte[40];
    System.arraycopy(exampleHashA, 0, array, 0, 20);
    System.arraycopy(exampleHashB, 0, array, 20, 20);
    Hash a = new Hash(exampleHashA);
    Hash b = new Hash(exampleHashB);
    Hash[] c = Hash.fromConcatenatedByteArrays(array);
    assertEquals(a, c[0]);
    assertEquals(b, c[1]);
    assertEquals(2, c.length);
  }

  public void testFromConcatenationNull() {
    try {
      Hash.fromConcatenatedByteArrays(null);
      fail("should fail");
    } catch (Exception e) {
      // expected
    }
  }

  public void testFromConcatenationWithInvalidLengths() {
    testWithLength(7);
    testWithLength(22);
    testWithLength(19);
    testWithLength(141);
  }

  public void testFromConcatenationWith0Bytes() {
    assertEquals(0, Hash.fromConcatenatedByteArrays(new byte[0]).length);
  }

  public void testEqualsHashOf() {
    byte[] bytes = ByteUtil.randomByteArray(128);
    Hash hash = Hash.forByteArray(bytes);
    assertTrue(hash.equalsHashOf(bytes, 0, 128));
  }

  private void testWithLength(int length) {
    byte[] array = new byte[length];
    try {
      Hash.fromConcatenatedByteArrays(array);
      fail("should fail");
    } catch (Exception e) {
      // expected
    }
  }

  public static Hash randomHash() {
    byte[] buffer = new byte[Hash.LENGTH];
    rnd.nextBytes(buffer);
    return new Hash(buffer);
  }
}
