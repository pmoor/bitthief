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
import ws.moor.bt.storage.BitField;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class BitFieldPacketTest extends ExtendedTestCase {

  private BitField bitField;

  private static final int PIECE_COUNT = 32;
  private BitFieldPacket packet;

  protected void setUp() throws Exception {
    super.setUp();
    bitField = new BitField(PIECE_COUNT);
    bitField.setRandomPieces(PIECE_COUNT / 2, rnd);
    packet = new BitFieldPacket(bitField);
  }

  public void testClassConstructor() {
    assertEquals(bitField, packet.getBitField());
  }

  public void testPayloadLength() {
    assertEquals(4, packet.getPayloadLength());
  }

  public void testID() {
    assertEquals(5, BitFieldPacket.ID);
    assertEquals(BitFieldPacket.ID, packet.getId());
  }

  public void testWriteToBuffer() {
    byte[] buffer = new byte[4];
    assertEquals(4, packet.writeIntoBuffer(buffer, 0));
    assertArrayEquals(bitField.toArray(), buffer);
  }

  public void testEquals() {
    BitFieldPacket packet2 = new BitFieldPacket(packet.getBitField());
    assertEquals(packet, packet2);
    assertEquals(packet.hashCode(), packet2.hashCode());
  }

  public void testConstructor() {
    PacketConstructor<BitFieldPacket> constructor = BitFieldPacket.getConstructor();
    assertEquals(5, constructor.getId());
    BitFieldPacket packet = constructor.constructPacket(
        ByteUtil.newByteArray(77, 0x10, 0x11, 0xf0, 0xff), 1, 3);

    BitField bitField = new BitField(24);
    bitField.gotPiece(3);
    bitField.gotPiece(11);
    bitField.gotPiece(15);
    bitField.gotPiece(16);
    bitField.gotPiece(17);
    bitField.gotPiece(18);
    bitField.gotPiece(19);
    assertEquals(new BitFieldPacket(bitField), packet);
  }

  public void testByteSize() {
    assertEquals(8, Byte.SIZE);
  }

  public void testHandler() {
    PacketHandler handler = EasyMock.createMock(PacketHandler.class);
    handler.handleBitFieldPacket(EasyMock.same(packet));
    EasyMock.replay(handler);
    packet.handle(handler);
    EasyMock.verify(handler);
  }

  public void testToString() {
    assertEquals(PIECE_COUNT / 2 + "/" + PIECE_COUNT, packet.toString());
  }
}
