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

import java.util.Arrays;

/**
 * TODO(pmoor): Javadoc
 */
public class PiecePacket implements Packet {

  public static final int ID = 7;

  private final int index;
  private final int begin;
  private final byte[] block;

  private static final int FIXED_PAYLOAD = 8;

  public PiecePacket(int index, int begin, byte[] block) {
    assertValidParameters(index, begin, block);
    this.index = index;
    this.begin = begin;
    this.block = block;
  }

  private void assertValidParameters(int index, int begin, byte[] block) {
    if (index < 0 || begin < 0) {
      throw new IllegalArgumentException();
    } else if (block == null) {
      throw new NullPointerException();
    }
  }

  public int writeIntoBuffer(byte[] buffer, int offset) {
    ByteUtil.int_to_b32(index, buffer, offset);
    ByteUtil.int_to_b32(begin, buffer, offset + 4);
    System.arraycopy(block, 0, buffer, offset + 8, block.length);
    return offset + getPayloadLength();
  }

  public int getPayloadLength() {
    return FIXED_PAYLOAD + block.length;
  }

  public int getId() {
    return ID;
  }

  public void handle(PacketHandler handler) {
    handler.handlePiecePacket(this);
  }

  public static PacketConstructor<PiecePacket> getConstructor() {
    return new Constructor();
  }

  public int getIndex() {
    return index;
  }

  public int getBegin() {
    return begin;
  }

  public byte[] getBlock() {
    return block;
  }

  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final PiecePacket that = (PiecePacket) o;
    if (begin != that.begin) {
      return false;
    }
    if (index != that.index) {
      return false;
    }
    return Arrays.equals(block, that.block);
  }

  public int hashCode() {
    return 29 * (29 * index + begin) + Arrays.hashCode(block);
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(index);
    builder.append(":");
    builder.append(begin);
    builder.append("-");
    builder.append(begin + block.length);
    return builder.toString();
  }

  private static class Constructor implements PacketConstructor<PiecePacket> {
    public int getId() {
      return ID;
    }

    public PiecePacket constructPacket(byte[] buffer, int offset, int length) {
      if (buffer.length < offset + length) {
        throw new IllegalArgumentException("buffer is too short");
      } else if (length < FIXED_PAYLOAD) {
        throw new IllegalArgumentException("too short");
      }
      int index = ByteUtil.b32_to_int(buffer, offset);
      int begin = ByteUtil.b32_to_int(buffer, offset + 4);
      byte[] block = new byte[length - FIXED_PAYLOAD];
      System.arraycopy(buffer, offset + FIXED_PAYLOAD, block, 0, block.length);
      return new PiecePacket(index, begin, block);
    }
  }
}
