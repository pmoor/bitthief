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

package ws.moor.bt.simulation.algebra;

import ws.moor.bt.simulation.Tuple;
import ws.moor.bt.util.CollectionUtils;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;

public class GaussianElimination {

  private static final int MATRIX_SIZE = 512;

  public static void main(String[] args) {
    Random rnd = new Random();

    SparseMatrix matrix = new SparseMatrix(MATRIX_SIZE, MATRIX_SIZE);
    for (int i = 1; i <= MATRIX_SIZE; i++) {
      Tuple tuple = Tuple.randomTuple(MATRIX_SIZE, 8);
      for (int j = 0; j < tuple.size(); j++) {
        matrix.set(i, tuple.get(j) + 1, 1);
      }
    }

    System.out.println(matrix.componentCount());
    //System.out.println(matrix);

    matrix.gaussianElimination();
    matrix.cleanNulls();
    
    System.out.println(matrix.componentCount());

    final int[] min = new int[] {Integer.MAX_VALUE};
    matrix.forEachNonNullValue(new CollectionUtils.Function<Integer, Void>() {
      public Void evaluate(Integer source) {
        if (source < min[0]) {
          min[0] = source;
        }
        return null;
      }
    });

    final int[] max = new int[] {Integer.MIN_VALUE};
    matrix.forEachNonNullValue(new CollectionUtils.Function<Integer, Void>() {
      public Void evaluate(Integer source) {
        if (source > max[0]) {
          max[0] = source;
        }
        return null;
      }
    });

    final int[] avg = new int[] {0, 0};
    matrix.forEachNonNullValue(new CollectionUtils.Function<Integer, Void>() {
      public Void evaluate(Integer source) {
        avg[0] += source;
        avg[1]++;
        return null;
      }
    });

    System.out.println("min = " + min[0]);
    System.out.println("max = " + max[0]);
    System.out.println("avg = " + avg[0]/avg[1]);

    final Map<Integer, Integer> counter = new HashMap<Integer, Integer>();
    matrix.forEachNonNullValue(new CollectionUtils.Function<Integer, Void>() {
      public Void evaluate(Integer source) {
        int bucket = source < 1000 ? 0 : source > SparseMatrix.MODULUS - 1000 ? 2 : 1;
        int value = counter.get(bucket) != null ? counter.get(bucket) : 0;
        value++;
        counter.put(bucket, value);
        return null;
      }
    });

    System.out.println(SparseMatrix.MODULUS);
    for (Map.Entry<Integer, Integer> entry : counter.entrySet()) {
      System.out.println(entry.getKey() + "\t" + entry.getValue());
    }
  }
}