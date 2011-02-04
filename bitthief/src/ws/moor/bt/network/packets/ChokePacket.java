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
public class ChokePacket implements Packet {

  public static final int ID = 0;

  private static final ChokePacket INSTANCE = new ChokePacket();

  public int writeIntoBuffer(byte[] buffer, int offset) {
    return offset;
  }

  public int getPayloadLength() {
    return 0;
  }

  public int getId() {
    return ID;
  }

  public void handle(PacketHandler handler) {
    handler.handleChokePacket(this);
  }

  public int hashCode() {
    return ChokePacket.class.hashCode();
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return ChokePacket.class == obj.getClass();
  }

  public static PacketConstructor<ChokePacket> getConstructor() {
    return new Constructor();
  }

  private static class Constructor implements PacketConstructor<ChokePacket> {
    public int getId() {
      return ID;
    }

    public ChokePacket constructPacket(byte[] buffer, int offset, int length) {
      if (length != 0) {
        throw new IllegalArgumentException("invalid length");
      }
      return INSTANCE;
    }
  }
}
