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

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RarestFirstDeterminerTest extends TestCase {

  private Information info;
  private RarestFirstDeterminer determiner;

  public void setUp() {
    info = new Information();
    determiner = new RarestFirstDeterminer(info);
  }

  public void testBla() {
    info.availableTuples.add(new Tuple(17, 32));
    info.availableTuples.add(new Tuple(14, 18));

    info.tupleCounts.put(new Tuple(1, 2), 1);
    info.tupleCounts.put(new Tuple(3, 4), 2);
    info.tupleCounts.put(new Tuple(5, 6), 3);


    Set<Tuple> set = new HashSet<Tuple>();
    set.add(new Tuple(1, 2));
    set.add(new Tuple(3, 4));
    set.add(new Tuple(5, 6));
    set.add(new Tuple(7, 8));
    set.add(new Tuple(17, 32));
    assertEquals(new Tuple(7, 8), determiner.getNextTuple(set));

    info.availableTuples.add(new Tuple(7, 8));
    assertEquals(new Tuple(1, 2), determiner.getNextTuple(set));

    info.availableTuples.add(new Tuple(1, 2));
    assertEquals(new Tuple(3, 4), determiner.getNextTuple(set));
    
    info.availableTuples.add(new Tuple(3, 4));
    assertEquals(new Tuple(5, 6), determiner.getNextTuple(set));

    info.availableTuples.add(new Tuple(5, 6));
    assertNull(determiner.getNextTuple(set));
  }

  private static class Information implements RarestFirstDeterminer.TupleInformationProvider {
    public final Set<Tuple> availableTuples = new HashSet<Tuple>();
    public final Map<Tuple, Integer> tupleCounts = new HashMap<Tuple, Integer>();

    public Set<Tuple> getAvailableTuples() {
      return availableTuples;
    }

    public Map<Tuple, Integer> getTupleCounts() {
      return tupleCounts;
    }

    public boolean isSeeder() {
      return false;
    }
  }
}
