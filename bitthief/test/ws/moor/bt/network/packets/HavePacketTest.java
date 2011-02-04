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
public class HavePacketTest extends ExtendedTestCase {

  private PacketConstructor<HavePacket> constructor;

  protected void setUp() throws Exception {
    super.setUp();
    constructor = HavePacket.getConstructor();
  }

  public void testConstruction() {
    HavePacket packet = new HavePacket(7412);
    assertEquals(7412, packet.getPieceIndex());
  }

  public void testEncoding() {
    HavePacket packet = new HavePacket(0x7421);
    byte[] buffer = new byte[5];
    int offset = packet.writeIntoBuffer(buffer, 1);
    assertEquals(5, offset);
    assertArrayEquals(ByteUtil.newByteArray(0, 0, 0, 0x74, 0x21), buffer);
  }

  public void testDecoding() {
    PacketConstructor<HavePacket> constructor = HavePacket.getConstructor();
    HavePacket packet = constructor.constructPacket(ByteUtil.newByteArray(7, 0, 0, 0, 42), 1, 4);
    assertEquals(42, packet.getPieceIndex());
  }

  public void testShortPacketDecoding() {
    try {
      constructor.constructPacket(ByteUtil.newByteArray(0, 0, 0, 42), 1, 4);
      fail("should not work");
    } catch (Exception e) {
      // expected
    }
  }

  public void testShortPacketDecodingWithCorrectLength() {
    try {
      constructor.constructPacket(ByteUtil.newByteArray(0, 0, 0, 42), 1, 3);
      fail("should not work");
    } catch (Exception e) {
      // expected
    }
  }

  public void testLongPacketDecoding() {
    try {
      constructor.constructPacket(ByteUtil.newByteArray(0, 0, 0, 42, 7), 0, 5);
      fail("should not work");
    } catch (Exception e) {
      // expected
    }
  }

  public void testPayloadLength() {
    assertEquals(4, new HavePacket(0).getPayloadLength());
  }

  public void testEquals() {
    HavePacket packetA = new HavePacket(77);
    HavePacket packetB = new HavePacket(77);
    HavePacket packetC = new HavePacket(55);
    assertEquals(packetA, packetB);
    assertNotEquals(packetA, packetC);
    assertEquals(packetA.hashCode(), packetB.hashCode());
  }

  public void testHandler() {
    HavePacket packet = new HavePacket(42);
    PacketHandler handler = EasyMock.createMock(PacketHandler.class);
    handler.handleHavePacket(EasyMock.same(packet));
    EasyMock.replay(handler);
    packet.handle(handler);
    EasyMock.verify(handler);
  }
}
