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
public class UnchokePacketTest extends ExtendedTestCase {

  public void testClassConstructor() {
    UnchokePacket packet = new UnchokePacket();
    UnchokePacket packet2 = new UnchokePacket();
    assertEquals(packet, packet2);
  }

  public void testID() {
    assertEquals(1, UnchokePacket.ID);
    assertEquals(1, new UnchokePacket().getId());
  }

  public void testEquals() {
    UnchokePacket packet = new UnchokePacket();
    assertEquals(packet, new UnchokePacket());
    assertEquals(new UnchokePacket().hashCode(), new UnchokePacket().hashCode());
    assertEquals(packet, packet);
  }

  public void testPayloadLength() {
    assertEquals(0, new UnchokePacket().getPayloadLength());
  }

  public void testConstructor() {
    PacketConstructor<UnchokePacket> constructor = UnchokePacket.getConstructor();
    assertEquals(1, constructor.getId());
    assertEquals(new UnchokePacket(), constructor.constructPacket(new byte[2], 1, 0));
  }

  public void testWriteInfoBuffer() {
    byte[] buffer = ByteUtil.randomByteArray(16);
    byte[] buffer2 = buffer.clone();
    assertEquals(7, new UnchokePacket().writeIntoBuffer(buffer2, 7));
    assertArrayEquals(buffer, buffer2);
  }

  public void testHandler() {
    UnchokePacket packet = new UnchokePacket();
    PacketHandler handler = EasyMock.createMock(PacketHandler.class);
    handler.handleUnchokePacket(EasyMock.same(packet));
    EasyMock.replay(handler);
    packet.handle(handler);
    EasyMock.verify(handler);
  }
}
