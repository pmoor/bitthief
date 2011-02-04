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

package ws.moor.bt;

import junit.framework.TestSuite;
import junit.textui.TestRunner;
import ws.moor.bt.bencoding.BEncodingTests;
import ws.moor.bt.dht.DHTests;
import ws.moor.bt.downloader.DownloaderTests;
import ws.moor.bt.et.ParameterSetTest;
import ws.moor.bt.network.NetworkTests;
import ws.moor.bt.stats.StatsTests;
import ws.moor.bt.storage.StorageTests;
import ws.moor.bt.torrent.HashTest;
import ws.moor.bt.torrent.MetaInfoBuilderTest;
import ws.moor.bt.torrent.PeerIdTest;
import ws.moor.bt.tracker.TrackerClientTest;
import ws.moor.bt.tracker.TrackerRequestTest;
import ws.moor.bt.tracker.TrackerResponseTest;
import ws.moor.bt.util.UtilTests;

/**
 * TODO(pmoor): Javadoc
 */
public class UnitTests {

  private static final Class[] testClasses = {
      HashTest.class,
      PeerIdTest.class,
      MetaInfoBuilderTest.class,
      ParameterSetTest.class,
      TrackerClientTest.class,
      TrackerRequestTest.class,
      TrackerResponseTest.class,
  };

  public static TestSuite suite() {
    TestSuite suite = new TestSuite("Unit Tests");

    for (Class testClass : testClasses) {
      suite.addTestSuite(testClass);
    }

    suite.addTest(BEncodingTests.suite());
    suite.addTest(DHTests.suite());
    suite.addTest(DownloaderTests.suite());
    suite.addTest(NetworkTests.suite());
    suite.addTest(StatsTests.suite());
    suite.addTest(StorageTests.suite());
    suite.addTest(UtilTests.suite());

    return suite;
  }

  public static void main(String[] args) {
    TestRunner.run(suite());
  }
}
