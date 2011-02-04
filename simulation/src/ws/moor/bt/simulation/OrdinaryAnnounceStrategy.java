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

package ws.moor.bt.simulation;

import java.util.Collection;

public class OrdinaryAnnounceStrategy implements TupleAnnounceStrategy {
  private final Node node;

  public OrdinaryAnnounceStrategy(Node node) {
    this.node = node;
  }

  public Collection<Tuple> getAvailableTuples(NetworkNode remote) {
    if (node.isLeecher()) {
      return node.getAvailableTuples();
    }

    return Tuple.randomTuplePermutation(node.getBlockCount(), remote.getId());
  }
}
