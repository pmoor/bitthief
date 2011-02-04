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

import ws.moor.bt.util.CollectionUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class SparseMatrix {

  private final int height;
  private final int width;

  private final SparseVector[] vectors;

  static final int MODULUS = BigInteger.probablePrime(15, new Random()).intValue();

  public SparseMatrix(int height, int width) {
    this.height = height;
    this.width = width;
    vectors = new SparseVector[height];
  }

  public int get(int row, int column) {
    assertValidField(row, column);
    return vectors[row - 1] != null ? vectors[row - 1].get(column) : 0;
  }

  public void set(int row, int column, int value) {
    assertValidField(row, column);
    if (vectors[row - 1] == null) {
      vectors[row - 1] = new SparseVector(width);
    }
    vectors[row - 1].set(column, value);
  }

  public void sortRows() {
    Arrays.sort(vectors, new Comparator<SparseVector>() {
      public int compare(SparseVector o1, SparseVector o2) {
        if (o1 == null && o2 != null) {
          return 1;
        } else if (o1 != null && o2 == null) {
          return -1;
        } else if (o1 == null && o2 == null) {
          return 0;
        }
        return o1.compareTo(o2);
      }
    });
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int row = 1; row <= height; row++) {
      if (row > 1) {
        builder.append("\n");
      }
      for (int column = 1; column <= width; column++) {
        if (column > 1) {
          builder.append("\t");
        }
        builder.append(get(row, column));
      }
    }
    return builder.toString();
  }

  public void gaussianElimination() {
    int pivotColumn = 1;
    int pivotRow = 1;
    while (pivotRow <= height && pivotColumn <= width) {
      sortRows();
      int pivotValue = get(pivotRow, pivotColumn);
      if (pivotValue == 0) {
        pivotColumn++;
        continue;
      }

      BigInteger i = BigInteger.valueOf(pivotValue);
      int inverse = i.modInverse(BigInteger.valueOf(MODULUS)).intValue();

      vectors[pivotRow - 1].multiply(inverse);
      vectors[pivotRow - 1].modulo(MODULUS);

      for (int row = pivotRow + 1; row <= height; row++) {
        int value = get(row, pivotColumn);
        if (value == 0) break;
        SparseVector vectorMultiple = new SparseVector(vectors[pivotRow - 1]);
        vectorMultiple.multiply(value);
        vectorMultiple.modulo(MODULUS);
        vectorMultiple.multiply(-1);
        vectorMultiple.add(MODULUS);
        vectors[row - 1].add(vectorMultiple);
        vectors[row - 1].modulo(MODULUS);
      }
      
      pivotRow++;
      pivotColumn++;
    }
  }

  public void cleanNulls() {
    for (SparseVector vector : vectors) {
      if (vector != null) {
        vector.cleanNulls();
      }
    }
  }

  public int componentCount() {
    final int[] count = new int[1];
    forEachNonNullValue(new CollectionUtils.Function<Integer, Object>() {
      public Object evaluate(Integer source) {
        count[0] += 1;
        return null;
      }
    });
    return count[0];
  }

  public void forEachNonNullValue(CollectionUtils.Function<Integer, ? extends Object> function) {
    for (SparseVector vector : vectors) {
      if (vector != null) {
        vector.forEachNonNullValue(function);
      }
    }
  }

  private void assertValidField(int row, int column) {
    if (row < 1 || row > height || column < 1 || column > width) {
      throw new IllegalArgumentException();
    }
  }
}
