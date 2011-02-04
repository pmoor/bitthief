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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AvailableTuplesPacket implements Packet {

  private final List<Tuple> tuples;

  private static final int BYTES_PER_TUPLE = 8;

  public AvailableTuplesPacket(Collection<Tuple> tuples) {
    this.tuples = new ArrayList<Tuple>(tuples);
  }

  public int getSizeOnWire() {
    return tuples.size() * BYTES_PER_TUPLE;
  }

  public void accept(PacketVisitor visitor) {
    visitor.visitAvailableTuplesPacket(this);
  }

  public Collection<Tuple> getTuples() {
    return tuples;
  }
}
