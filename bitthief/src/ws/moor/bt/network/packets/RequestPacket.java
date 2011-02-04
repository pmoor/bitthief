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

import ws.moor.bt.downloader.Block;
import ws.moor.bt.util.ByteUtil;

/**
 * TODO(pmoor): Javadoc
 */
public class RequestPacket extends AbstractRequestCancelPacket {

  public static final int ID = 6;

  public RequestPacket(int index, int begin, int length) {
    super(index, begin, length);
  }

  public RequestPacket(Block block) {
    this(block.getPieceIndex(), block.getOffset(), block.getLength());
  }

  public int getId() {
    return ID;
  }

  public void handle(PacketHandler handler) {
    handler.handleRequestPacket(this);
  }

  public static PacketConstructor<RequestPacket> getConstructor() {
    return new Constructor();
  }

  private static class Constructor implements PacketConstructor<RequestPacket> {
    public int getId() {
      return ID;
    }

    public RequestPacket constructPacket(byte[] buffer, int offset, int length) {
      if (buffer.length < offset + length) {
        throw new IllegalArgumentException("buffer too small");
      } else if (length != PAYLOAD_LENGTH) {
        throw new IllegalArgumentException("wrong length");
      }
      int index = ByteUtil.b32_to_int(buffer, offset);
      int begin = ByteUtil.b32_to_int(buffer, offset + 4);
      int len = ByteUtil.b32_to_int(buffer, offset + 8);
      return new RequestPacket(index, begin, len);
    }
  }
}
