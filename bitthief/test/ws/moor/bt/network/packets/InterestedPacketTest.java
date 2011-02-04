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

import org.easymock.classextension.EasyMock;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class InterestedPacketTest extends ExtendedTestCase {

  public void testClassConstructor() {
    InterestedPacket packet = new InterestedPacket();
    InterestedPacket packet2 = new InterestedPacket();
    assertEquals(packet, packet2);
  }

  public void testID() {
    assertEquals(2, InterestedPacket.ID);
    assertEquals(2, new InterestedPacket().getId());
  }

  public void testEquals() {
    InterestedPacket packet = new InterestedPacket();
    assertEquals(packet, new InterestedPacket());
    assertEquals(new InterestedPacket().hashCode(), new InterestedPacket().hashCode());
    assertEquals(packet, packet);
  }

  public void testPayloadLength() {
    assertEquals(0, new InterestedPacket().getPayloadLength());
  }

  public void testConstructor() {
    PacketConstructor<InterestedPacket> constructor = InterestedPacket.getConstructor();
    assertEquals(2, constructor.getId());
    assertEquals(new InterestedPacket(), constructor.constructPacket(new byte[2], 1, 0));
  }

  public void testWriteInfoBuffer() {
    byte[] buffer = ByteUtil.randomByteArray(16);
    byte[] buffer2 = buffer.clone();
    assertEquals(7, new InterestedPacket().writeIntoBuffer(buffer2, 7));
    assertArrayEquals(buffer, buffer2);
  }

  public void testHandler() {
    InterestedPacket packet = new InterestedPacket();
    PacketHandler handler = EasyMock.createMock(PacketHandler.class);
    handler.handleInterestedPacket(EasyMock.same(packet));
    EasyMock.replay(handler);
    packet.handle(handler);
    EasyMock.verify(handler);
  }
}
