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
public class NotInterestedPacket implements Packet {

  public static final int ID = 3;

  private static final NotInterestedPacket instance = new NotInterestedPacket();

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
    handler.handleNotInterestedPacket(this);
  }

  public int hashCode() {
    return NotInterestedPacket.class.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return obj.getClass() == NotInterestedPacket.class;
  }

  public static PacketConstructor<NotInterestedPacket> getConstructor() {
    return new NotInterestedPacket.Constructor();
  }

  private static class Constructor implements PacketConstructor<NotInterestedPacket> {
    public int getId() {
      return ID;
    }

    public NotInterestedPacket constructPacket(byte[] buffer, int offset, int length) {
      return NotInterestedPacket.instance;
    }
  }
}
