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
public class UnchokePacket implements Packet {

  public static final int ID = 1;

  private static final UnchokePacket INSTANCE = new UnchokePacket();

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
    handler.handleUnchokePacket(this);
  }

  public int hashCode() {
    return UnchokePacket.class.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return obj.getClass() == UnchokePacket.class;
  }

  public static PacketConstructor<UnchokePacket> getConstructor() {
    return new UnchokePacket.Constructor();
  }

  private static class Constructor implements PacketConstructor<UnchokePacket> {
    public int getId() {
      return ID;
    }

    public UnchokePacket constructPacket(byte[] buffer, int offset, int length) {
      return UnchokePacket.INSTANCE;
    }
  }
}
