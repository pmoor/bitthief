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

import junit.framework.TestSuite;

/**
 * TODO(pmoor): Javadoc
 */
public class StorageTests {

  private static final Class[] testClasses = {
      BitFieldTest.class,
      DataBlockTest.class,
      PartialPieceContainerTest.class,
      PieceManagerTest.class,
      StorageTest.class,
      VirtualFileRepositoryTest.class,
  };

  public static TestSuite suite() {
    TestSuite suite = new TestSuite("Storage Tests");

    for (Class testClass : StorageTests.testClasses) {
      suite.addTestSuite(testClass);
    }

    return suite;
  }
}
