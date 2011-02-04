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
public class PiecePacketTest extends ExtendedTestCase {

  private PiecePacket packet;
  private byte[] block;
  private static final int BLOCK_SIZE = 32;

  protected void setUp() throws Exception {
    super.setUp();
    block = ByteUtil.randomByteArray(BLOCK_SIZE);
    packet = new PiecePacket(1, 2, block);
  }

  public void testClassConstructor() {
    assertEquals(1, packet.getIndex());
    assertEquals(2, packet.getBegin());
    assertArrayEquals(block, packet.getBlock());
  }

  public void testNegativeIndex() {
    try {
      new PiecePacket(-1, 2, block);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testNegativeBegin() {
    try {
      new PiecePacket(0, -2, block);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testNullBlock() {
    try {
      new PiecePacket(3, 2, null);
      fail("should fail");
    } catch (NullPointerException e) {
      // expected
    }
  }

  public void testPayloadLength() {
    assertEquals(8 + BLOCK_SIZE, packet.getPayloadLength());
  }

  public void testID() {
    assertEquals(7, PiecePacket.ID);
    assertEquals(PiecePacket.ID, packet.getId());
  }

  public void testWriteToBuffer() {
    byte[] buffer = new byte[BLOCK_SIZE + 8 + 1];
    assertEquals(1 + BLOCK_SIZE + 8, packet.writeIntoBuffer(buffer, 1));
    byte[] expected = new byte[buffer.length];
    expected[4] = 1;
    expected[8] = 2;
    System.arraycopy(block, 0, expected, 9, block.length);
    assertArrayEquals(expected, buffer);
  }

  public void testEquals() {
    PiecePacket packet2 = new PiecePacket(1, 2, block.clone());
    assertEquals(packet, packet2);
    assertEquals(packet.hashCode(), packet2.hashCode());
  }

  public void testConstructor() {
    PacketConstructor<PiecePacket> constructor = PiecePacket.getConstructor();
    assertEquals(7, constructor.getId());
    PiecePacket packet = constructor.constructPacket(
        ByteUtil.newByteArray(0x42, 0x41, 0x40, 0x39, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32, 0x31), 0, 12);

    assertEquals(new PiecePacket(0x42414039, 0x38373635, ByteUtil.newByteArray(0x34, 0x33, 0x32, 0x31)), packet);
  }

  public void testHandler() {
    PacketHandler handler = EasyMock.createMock(PacketHandler.class);
    handler.handlePiecePacket(EasyMock.same(packet));
    EasyMock.replay(handler);
    packet.handle(handler);
    EasyMock.verify(handler);
  }

  public void testToString() {
    assertEquals("7:10-30", new PiecePacket(7, 10, new byte[20]).toString());
  }
}
