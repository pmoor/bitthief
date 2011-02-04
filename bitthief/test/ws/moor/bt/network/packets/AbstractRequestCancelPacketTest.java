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

import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public abstract class AbstractRequestCancelPacketTest extends ExtendedTestCase {

  protected AbstractRequestCancelPacket packet;

  protected void setUp() throws Exception {
    super.setUp();
    packet = constructPacket(1, 2, 3);
  }

  protected abstract AbstractRequestCancelPacket constructPacket(int index, int begin, int length);

  public void testClassConstructor() {
    assertEquals(1, packet.getIndex());
    assertEquals(2, packet.getBegin());
    assertEquals(3, packet.getLength());
  }

  public void testNegativeIndex() {
    try {
      constructPacket(-1, 2, 3);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testNegativeBegin() {
    try {
      constructPacket(0, -2, 3);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testNegativeLength() {
    try {
      constructPacket(3, 2, -3);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testPayloadLength() {
    assertEquals(12, packet.getPayloadLength());
  }

  public void testWriteToBuffer() {
    byte[] buffer = new byte[13];
    assertEquals(13, packet.writeIntoBuffer(buffer, 1));
    assertArrayEquals(ByteUtil.newByteArray(0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3), buffer);
  }

  public void testEquals() {
    AbstractRequestCancelPacket packet2 = constructPacket(1, 2, 3);
    assertEquals(packet, packet2);
    assertEquals(packet.hashCode(), packet2.hashCode());
  }

  public void testConstructor() {
    PacketConstructor<? extends AbstractRequestCancelPacket> constructor = constructConstructor();
    AbstractRequestCancelPacket packet = constructor.constructPacket(
        ByteUtil.newByteArray(0x42, 0x41, 0x40, 0x39, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32, 0x31), 0, 12);

    assertEquals(constructPacket(0x42414039, 0x38373635, 0x34333231), packet);
  }

  public void testToString() {
    AbstractRequestCancelPacket packet = constructPacket(50, 80, 100);
    assertEquals("50:80-180", packet.toString());
  }

  protected abstract PacketConstructor<? extends AbstractRequestCancelPacket> constructConstructor();
}
