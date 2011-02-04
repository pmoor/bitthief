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

import junit.framework.TestCase;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

/**
 * TODO(pmoor): Javadoc
 */
public class ExtendedTestCase extends TestCase {

  protected static Random rnd = new Random();

  protected void setUp() throws Exception {
    super.setUp();
  }

  public static void assertArrayEquals(Object[] expected, Object[] actual) {
    assertTrue(Arrays.equals(expected, actual));
  }

  public static void assertArrayEquals(byte[] expected, byte[] actual) {
    if (!Arrays.equals(expected, actual)) {
      fail("expected: " + Arrays.toString(expected) + " but got: " + Arrays.toString(actual));
    }
  }

  public static void assertInstanceof(Class<?> aClass, Object anObject) {
    assertTrue(aClass.isAssignableFrom(anObject.getClass()));
  }

  public static void assertNotEquals(Object a, Object b) {
    assertFalse(a.equals(b));
  }

  public static void assertArrayEquals(double[] expected, double[] actual) {
    assertTrue(Arrays.equals(expected, actual));
  }

  public static <E> void assertContains(E o, Iterable<E> iterable) {
    if (!containedIn(o, iterable)) {
      fail(o + " not found in " + iterable);
    }
  }

  public static <E> void assertContainsNot(E o, Iterable<E> iterable) {
    if (containedIn(o, iterable)) {
      fail(o + " unexpectedly found in " + iterable);
    }
  }

  private static <E> boolean containedIn(E o, Iterable<E> iterable) {
    for (E e : iterable) {
      if (o == null && e == null || o != null && o.equals(e)) {
        return true;
      }
    }
    return false;
  }

  public static InetSocketAddress randomInetSocketAddress() {
    return new InetSocketAddress(randomInetAddress(), randomPort());
  }

  public static int randomPort() {
    return rnd.nextInt(50000) + 2000;
  }

  public static InetAddress randomInetAddress() {
    try {
      return InetAddress.getByAddress(ByteUtil.randomByteArray(4));
    } catch (UnknownHostException e) {
      fail("should not happen");
      return null;
    }
  }
}
