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

/**
 * TODO(pmoor): Javadoc
 */
public class KeepAlivePacket implements Packet {

  private static final KeepAlivePacket instance = new KeepAlivePacket();

  public int writeIntoBuffer(byte[] buffer, int offset) {
    return offset;
  }

  public int getPayloadLength() {
    return 0;
  }

  public int getId() {
    throw new UnsupportedOperationException("this packet does not have an ID");
  }

  public void handle(PacketHandler handler) {
    handler.handleKeepAlivePacket(this);
  }

  public int hashCode() {
    return KeepAlivePacket.class.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return obj.getClass() == KeepAlivePacket.class;
  }

  public static PacketConstructor<KeepAlivePacket> getConstructor() {
    return new Constructor();
  }

  private static class Constructor implements PacketConstructor<KeepAlivePacket> {
    public int getId() {
      throw new UnsupportedOperationException("this packet type does not have an ID");
    }

    public KeepAlivePacket constructPacket(byte[] buffer, int offset, int length) {
      return instance;
    }
  }
}
