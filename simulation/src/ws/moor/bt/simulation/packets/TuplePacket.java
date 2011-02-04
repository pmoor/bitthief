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

package ws.moor.bt.simulation.packets;

import ws.moor.bt.simulation.Tuple;

public class TuplePacket implements Packet {

  private final Tuple tuple;
  private final boolean free;

  private static final int SIZE_PER_TUPLE = 128 * 1024;

  public TuplePacket(Tuple tuple, boolean free) {
    this.tuple = tuple;
    this.free = free;
  }

  public void accept(PacketVisitor visitor) {
    visitor.visitTuplePacket(this);
  }

  public int getSizeOnWire() {
    return SIZE_PER_TUPLE;
  }

  public Tuple getTuple() {
    return tuple;
  }

  public boolean isFree() {
    return free;
  }
}
