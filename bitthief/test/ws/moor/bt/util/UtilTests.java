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

import junit.framework.TestSuite;

/**
 * TODO(pmoor): Javadoc
 */
public class UtilTests {

  private static final Class[] testClasses = {
      ArrayUtilTest.class,
      ByteUtilTest.class,
      CacheMapTest.class,
      CacheSetTest.class,
      CollectionUtilsTest.class,
      DigestUtilTest.class,
      ExtendedTestCaseTest.class,
      PrefixLoggerTest.class,
      StreamUtilTest.class,
      StringUtilTest.class,
      SystemTimeSourceTest.class,
      TimeoutSetTest.class,
  };

  public static TestSuite suite() {
    TestSuite suite = new TestSuite("Util Tests");

    for (Class testClass : UtilTests.testClasses) {
      suite.addTestSuite(testClass);
    }

    return suite;
  }
}
