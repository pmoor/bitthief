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

import junit.framework.AssertionFailedError;

import java.util.Arrays;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class ExtendedTestCaseTest extends ExtendedTestCase {

  public void testAssertEqualsForArrays() {
    byte[] a = new byte[32];
    byte[] b = new byte[32];
    rnd.nextBytes(a);
    System.arraycopy(a, 0, b, 0, a.length);
    assertArrayEquals(a, b);
    assertArrayEquals(b, a);
  }

  public void testAssertEqualsForArraysFail() {
    byte[] a = new byte[32];
    byte[] b = new byte[32];
    rnd.nextBytes(a);
    System.arraycopy(a, 0, b, 0, a.length);
    a[16] = (byte) (b[16] - 1);

    boolean failed;
    try {
      assertArrayEquals(a, b);
      failed = false;
    } catch (AssertionFailedError e) {
      // expected
      failed = true;
    }
    if (!failed) {
      fail("should have failed");
    }

    failed  = false;
    try {
      assertArrayEquals(b, a);
      failed = false;
    } catch (AssertionFailedError e) {
      // expected
      failed = true;
    }
    if (!failed) {
      fail("should have failed");
    }
  }

  public void testAssertNotEquals() {
    assertNotEquals(7, 3);
    assertNotEquals("bla", "bla2");
    assertNotEquals(new Object(), new Object());
  }

  public void testAssertNotEqualsFailing() {
    try {
      assertNotEquals(3, 3);
      fail("should fail");
    } catch (AssertionFailedError e) {
      // expected
    }
  }

  public void testAssertContains() {
    List<Integer> list = Arrays.asList(7, 2, 18, 33, 48, null);
    assertContains(7, list);
    assertContains(2, list);
    assertContains(18, list);
    assertContains(33, list);
    assertContains(48, list);
    assertContains(null, list);
  }

  public void testFailingAssertContains() {
    List<Integer> list = Arrays.asList(7, 2, 18, 33, 48);
    try {
      assertContains(5, list);
      fail("should fail");
    } catch (AssertionFailedError e) {
      // expected
    }
    try {
      assertContains(null, list);
      fail("should fail");
    } catch (AssertionFailedError e) {
      // expected
    }
  }
  
  public void testAssertContainsNot() {
    List<Integer> list = Arrays.asList(7, 2, 18, 33, 48);
    assertContainsNot(4, list);
    assertContainsNot(3, list);
    assertContainsNot(19, list);
    assertContainsNot(32, list);
    assertContainsNot(50, list);
    assertContainsNot(null, list);

    list = Arrays.asList(14, null);
    assertContainsNot(5, list);
  }

  public void testFailingAssertContainsNot() {
    List<Integer> list = Arrays.asList(7, 2, 18, 33, 48, null);
    try {
      assertContainsNot(7, list);
      fail();
    } catch (AssertionFailedError e) {
      // expected
    }
    try {
      assertContainsNot(0, list);
      fail();
    } catch (AssertionFailedError e) {
      // expected
    }
  }

  public void testDoubleArrayEquals() {
    assertArrayEquals(new double[0], new double[0]);
    assertArrayEquals(new double[] {1}, new double[] {1});
    assertArrayEquals(new double[] {1, 7, 8}, new double[] {1, 7, 8});
  }
}
