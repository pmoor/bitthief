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
public class ChokePacketTest extends ExtendedTestCase {

  private ChokePacket packet;

  public void testClassConstructor() {
    ChokePacket packet2 = new ChokePacket();
    assertEquals(packet, packet2);
  }

  public void testID() {
    assertEquals(0, ChokePacket.ID);
    assertEquals(0, packet.getId());
  }

  public void testEquals() {
    ChokePacket packet2 = new ChokePacket();
    assertEquals(packet, packet);
    assertEquals(packet, packet2);
    assertEquals(packet.hashCode(), packet2.hashCode());
  }

  public void testPayloadLength() {
    assertEquals(0, packet.getPayloadLength());
  }

  public void testConstructor() {
    PacketConstructor<ChokePacket> constructor = ChokePacket.getConstructor();
    assertEquals(0, constructor.getId());
    assertEquals(packet, constructor.constructPacket(new byte[2], 1, 0));
  }

  public void testWriteInfoBuffer() {
    byte[] buffer = ByteUtil.randomByteArray(16);
    byte[] buffer2 = buffer.clone();
    assertEquals(7, packet.writeIntoBuffer(buffer2, 7));
    assertArrayEquals(buffer, buffer2);
  }

  public void testHandler() {
    PacketHandler handler = EasyMock.createMock(PacketHandler.class);
    handler.handleChokePacket(EasyMock.same(packet));
    EasyMock.replay(handler);
    packet.handle(handler);
    EasyMock.verify(handler);
  }

  protected void setUp() throws Exception {
    super.setUp();
    packet = new ChokePacket();
  }
}
