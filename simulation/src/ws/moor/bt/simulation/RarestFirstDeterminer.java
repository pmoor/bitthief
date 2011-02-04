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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RarestFirstDeterminer implements NextTupleDeterminer {

  private final TupleInformationProvider informationProvider;

  public RarestFirstDeterminer(TupleInformationProvider node) {
    this.informationProvider = node;
  }

  public Tuple getNextTuple(Set<Tuple> remoteTuples) {
    if (informationProvider.isSeeder()) {
      return null;
    }
    List<Tuple> interestingTuples = new ArrayList<Tuple>(remoteTuples);
    interestingTuples.removeAll(informationProvider.getAvailableTuples());

    Collections.shuffle(interestingTuples);
    Collections.sort(interestingTuples, new CleverComparator(informationProvider.getTupleCounts()));

    if (interestingTuples.size() > 0) {
      return interestingTuples.get(0);
    }

    return null;
  }

  public interface TupleInformationProvider {
    public boolean isSeeder();
    public Set<Tuple> getAvailableTuples();
    public Map<Tuple, Integer> getTupleCounts();
  }

  private static class CleverComparator implements Comparator<Tuple> {
    private final Map<Tuple, Integer> counts;

    public CleverComparator(Map<Tuple, Integer> counts) {
      this.counts = counts;
    }

    public int compare(Tuple t1, Tuple t2) {
      int c1 = getCount(t1);
      int c2 = getCount(t2);
      return c1 - c2;
    }

    private int getCount(Tuple tuple) {
      Integer count = counts.get(tuple);
      return count != null ? count : 0;
    }
  }
}
