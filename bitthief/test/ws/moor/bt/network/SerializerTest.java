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

package ws.moor.bt.network;

import org.easymock.classextension.EasyMock;
import ws.moor.bt.network.packets.HandshakePacket;
import ws.moor.bt.network.packets.KeepAlivePacket;
import ws.moor.bt.network.packets.Packet;
import ws.moor.bt.network.packets.PacketFactory;
import ws.moor.bt.torrent.HashTest;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class SerializerTest extends ExtendedTestCase {

  private Serializer serializer;
  private PacketFactory packetFactory;

  protected void setUp() throws Exception {
    super.setUp();
    packetFactory = EasyMock.createMock(PacketFactory.class);
    serializer = new Serializer(packetFactory);
  }

  protected void tearDown() throws Exception {
    EasyMock.verify(packetFactory);
    super.tearDown();
  }

  public void testEmpty() {
    EasyMock.replay(packetFactory);
    assertEquals(0, serializer.dataAvailable());
  }

  public void testASmallPacket() {
    KeepAlivePacket packet = new KeepAlivePacket();
    EasyMock.expect(packetFactory.getExpectedBytesOnWire(EasyMock.same(packet))).andReturn(4);
    packetFactory.serializePacket(EasyMock.same(packet), (byte[]) EasyMock.anyObject(), EasyMock.eq(0));
    EasyMock.replay(packetFactory);

    serializer.addPacket(packet);
    assertEquals(4, serializer.dataAvailable());
    assertArrayEquals(new byte[4], serializer.lendBytes(4));
    assertEquals(4, serializer.dataAvailable());
    serializer.confirmWrittenBytes(new byte[4], 4);
    assertEquals(0, serializer.dataAvailable());
  }

  public void testLendMoreThanAvailable() {
    EasyMock.replay(packetFactory);
    assertEquals(0, serializer.lendBytes(2).length);
  }

  public void testConfirmWrongBytes() {
    Packet packet = new HandshakePacket(HashTest.randomHash(), PeerId.createRandomMainlineId());
    EasyMock.expect(packetFactory.getExpectedBytesOnWire(EasyMock.same(packet))).andReturn(68);
    packetFactory.serializePacket(EasyMock.same(packet), (byte[]) EasyMock.anyObject(), EasyMock.eq(0));
    EasyMock.replay(packetFactory);

    serializer.addPacket(packet);
    assertEquals(68, serializer.dataAvailable());

    byte[] buffer = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
    try {
      serializer.confirmWrittenBytes(buffer, 6);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
