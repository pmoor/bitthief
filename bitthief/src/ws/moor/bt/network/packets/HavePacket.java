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

/**
 * TODO(pmoor): Javadoc
 */
public class HavePacket implements Packet {

  public static final int ID = 4;

  private static final int PAYLOAD_LENGTH = 4;

  private final int pieceIndex;

  public HavePacket(int pieceIndex) {
    this.pieceIndex = pieceIndex;
  }

  public int writeIntoBuffer(byte[] buffer, int offset) {
    if (buffer.length < offset + getPayloadLength()) {
      throw new IllegalArgumentException("buffer is too small");
    }
    ByteUtil.int_to_b32(pieceIndex, buffer, offset);
    return offset + PAYLOAD_LENGTH;
  }

  public int getPayloadLength() {
    return PAYLOAD_LENGTH;
  }

  public int getId() {
    return ID;
  }

  public void handle(PacketHandler handler) {
    handler.handleHavePacket(this);
  }

  public static PacketConstructor<HavePacket> getConstructor() {
    return new HavePacket.Constructor();
  }

  public int getPieceIndex() {
    return pieceIndex;
  }

  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return pieceIndex == ((HavePacket) o).pieceIndex;
  }

  public int hashCode() {
    return pieceIndex;
  }

  public String toString() {
    return Integer.toString(pieceIndex);
  }

  private static class Constructor implements PacketConstructor<HavePacket> {
    public int getId() {
      return ID;
    }

    public HavePacket constructPacket(byte[] buffer, int offset, int length) {
      if (length != PAYLOAD_LENGTH || buffer.length < offset + length) {
        throw new IllegalArgumentException("buffer length is not correct");
      }
      int pieceIndex = ByteUtil.b32_to_int(buffer, offset);
      return new HavePacket(pieceIndex);
    }
  }
}
