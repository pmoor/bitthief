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

import ws.moor.bt.storage.BitField;

/**
 * TODO(pmoor): Javadoc
 */
public class BitFieldPacket implements Packet {

  public static final int ID = 5;

  private final BitField bitField;

  public BitFieldPacket(BitField bitField) {
    this.bitField = bitField.clone();
  }

  public int writeIntoBuffer(byte[] buffer, int offset) {
    int payload = bitField.getByteCount();
    if (buffer.length < offset + payload) {
      throw new IllegalArgumentException("buffer is too small");
    }
    System.arraycopy(bitField.toArray(), 0, buffer, offset, payload);
    return offset + payload;
  }

  public int getPayloadLength() {
    return bitField.getByteCount();
  }

  public int getId() {
    return ID;
  }

  public void handle(PacketHandler handler) {
    handler.handleBitFieldPacket(this);
  }

  public BitField getBitField() {
    return bitField.clone();
  }

  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return bitField.equals(((BitFieldPacket) o).bitField);
  }

  public int hashCode() {
    return bitField.hashCode();
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(bitField.getAvailablePieceCount());
    builder.append("/");
    builder.append(bitField.getPieceCount());
    return builder.toString();
  }

  public static PacketConstructor<BitFieldPacket> getConstructor() {
    return new Constructor();
  }

  private static class Constructor implements PacketConstructor<BitFieldPacket> {
    public int getId() {
      return ID;
    }

    public BitFieldPacket constructPacket(byte[] buffer, int offset, int length) {
      BitField bitField = BitField.fromArray(buffer, offset, length);
      return new BitFieldPacket(bitField);
    }
  }
}
