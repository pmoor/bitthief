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

import ws.moor.bt.util.ArrayUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Tuple {

  private final int[] e;

  private static Random rnd = new Random();
  private static final int K = 1; // 12;

  public Tuple(int ... elements) {
    e = elements.clone();
    Arrays.sort(e);
  }

  public Tuple(Tuple other) {
    e = other.e;
  }

  public int size() {
    return e.length;
  }

  public int get(int index) {
    return e[index];
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("{");
    boolean first = true;
    for (int i : e) {
      if (!first) {
        builder.append(",");
      } else {
        first = false;
      }
      builder.append(i);
    }
    builder.append("}");
    return builder.toString();
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return Arrays.equals(e, ((Tuple) o).e);
  }

  public int hashCode() {
    int code = 7;
    for (int element : e) {
      code = (code * 11 + element) ^ code;
    }
    return code;
  }

  public static Tuple randomTuple(int blockCount) {
    return randomTuple(blockCount, 3);
  }

  public static Tuple randomTuple(int blockCount, int tupleSize) {
    int[] blocks = new int[tupleSize];
    Arrays.fill(blocks, -1);
    for (int i = 0; i < blocks.length;) {
      int candidate = rnd.nextInt(blockCount);
      if (ArrayUtil.contains(candidate, blocks)) {
        continue;
      }
      blocks[i++] = candidate;
    }
    return new Tuple(blocks);
  }

  public static Set<Tuple> randomTuplePermutation(int blockCount, long seed) {
    Integer[] allBlocks = new Integer[blockCount + K - 1];
    for (int i = 0; i < blockCount; i++) {
      allBlocks[i] = i;
    }
    Collections.shuffle(Arrays.asList(allBlocks).subList(0, blockCount), new Random(seed));
    System.arraycopy(allBlocks, 0, allBlocks, blockCount, K - 1);

    Set<Tuple> tuples = new HashSet<Tuple>(blockCount / K);
    int[] tupleComponents = new int[K];
    for (int j = 0; j < blockCount; j += K) {
      for (int i = 0; i < K; i++) {
        tupleComponents[i] = allBlocks[j + i];
      }
      tuples.add(new Tuple(tupleComponents));
    }

    return tuples;
  }

  public static Set<Tuple> randomSingleTuples(int blockCount, long seed) {
    Random rnd = new Random(seed);
    Set<Tuple> tuples = new HashSet<Tuple>(blockCount);
    while (tuples.size() < blockCount /3) {
      tuples.add(new Tuple(rnd.nextInt(blockCount)));
    }
    return tuples;
  }

  public int getFrequencyScore(int[] blockFrequency) {
    int score = 0;
    for (int i = 0; i < size(); i++) {
      int freq = blockFrequency[e[i]];
      score += (freq > 0) ? freq + 0xffff : 0;
    }
    return score;
  }
}
