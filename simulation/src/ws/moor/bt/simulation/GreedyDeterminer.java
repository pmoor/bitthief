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
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GreedyDeterminer implements NextTupleDeterminer {

  private final Node node;

  public GreedyDeterminer(Node node) {
    this.node = node;
  }

  public Tuple getNextTuple(Set<Tuple> remoteTuples) {
    if (node.isSeeder()) {
      return null;
    }
    Set<Tuple> availableTuples = node.getAvailableTuples();

    int[] blockFrequency = node.getBlockFrequencies();

    int lowestScore = Integer.MAX_VALUE;
    Tuple lowestScoringTuple = null;
    for (Tuple tuple : randomizeSet(remoteTuples)) {
      if (availableTuples.contains(tuple)) {
        continue;
      }
      int score = tuple.getFrequencyScore(blockFrequency);
      if (score < lowestScore) {
        lowestScore = score;
        lowestScoringTuple = tuple;
      }
      if (score == 0) {
        break;
      }
    }

    return lowestScoringTuple;
  }

  private Iterable<Tuple> randomizeSet(final Set<Tuple> set) {
    return new Iterable<Tuple>() {
      public Iterator<Tuple> iterator() {
        return new Iterator<Tuple>() {
          private int listSize = set.size();
          private int position = 0;
          private Random rnd = new Random();
          private List<Tuple> tuples = new ArrayList<Tuple>(set);

          public boolean hasNext() {
            return position < listSize;
          }

          public Tuple next() {
            swap(position, position + rnd.nextInt(listSize - position));
            return tuples.get(position++);
          }

          private void swap(int a, int b) {
            Tuple temp = tuples.get(a);
            tuples.set(a, tuples.get(b));
            tuples.set(b, temp);
          }

          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
}
