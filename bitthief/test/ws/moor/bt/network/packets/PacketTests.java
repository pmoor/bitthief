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

package ws.moor.bt.network.packets;

import junit.framework.TestSuite;

/**
 * TODO(pmoor): Javadoc
 */
public class PacketTests extends TestSuite {

  private static final Class[] testClasses = {
      BitFieldPacketTest.class,
      CancelPacketTest.class,
      ChokePacketTest.class,
      HandshakePacketTest.class,
      HavePacketTest.class,
      InterestedPacketTest.class,
      KeepAlivePacketTest.class,
      NotInterestedPacketTest.class,
      PacketFactoryTest.class,
      PiecePacketTest.class,
      RequestPacketTest.class,
      UnchokePacketTest.class,
  };

  public static TestSuite suite() {
    TestSuite suite = new TestSuite("Packet Tests");

    for (Class testClass : testClasses) {
      suite.addTestSuite(testClass);
    }

    return suite;
  }
}
