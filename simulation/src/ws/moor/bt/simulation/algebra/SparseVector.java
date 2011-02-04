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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SparseVector implements Comparable<SparseVector> {

  private final int dimension;

  private int firstComponent = Integer.MAX_VALUE;

  private int nullElement = 0;

  private final Map<Integer, MutableInteger> values = new HashMap<Integer, MutableInteger>();

  public SparseVector(int dimension) {
    this.dimension = dimension;
  }

  public SparseVector(SparseVector vector) {
    this.dimension = vector.dimension;
    for (Map.Entry<Integer, MutableInteger> entry : vector.values.entrySet()) {
      this.values.put(entry.getKey(), new MutableInteger(entry.getValue().value));
    }
  }

  public int get(int component) {
    assertValidComponent(component);
    MutableInteger value = values.get(component);
    return value != null ? value.value : nullElement;
  }

  public void set(int component, int value) {
    assertValidComponent(component);
    if (value == nullElement) {
      values.remove(component);
      firstComponent = Integer.MAX_VALUE;
    } else {
      MutableInteger existing = values.get(component);
      if (existing == null) {
        values.put(component, new MutableInteger(value));
        firstComponent = Integer.MAX_VALUE;
      } else {
        existing.value = value;
      }
    }
  }

  public void add(SparseVector vector) {
    for (Map.Entry<Integer, MutableInteger> component : vector.values.entrySet()) {
      int existing = get(component.getKey());
      set(component.getKey(), existing + component.getValue().value);
    }
    firstComponent = Integer.MAX_VALUE;
  }

  public void subtract(SparseVector vector) {
    for (Map.Entry<Integer, MutableInteger> component : vector.values.entrySet()) {
      int existing = get(component.getKey());
      set(component.getKey(), existing - component.getValue().value);
    }
    firstComponent = Integer.MAX_VALUE;
  }

  public void multiply(int multiplicator) {
    for (Map.Entry<Integer, MutableInteger> entry : values.entrySet()) {
      entry.getValue().value *= multiplicator;
    }
    nullElement *= multiplicator;
    cleanNulls();
  }

  public void modulo(int modulus) {
    for (Map.Entry<Integer, MutableInteger> entry : values.entrySet()) {
      entry.getValue().value %= modulus;
    }
    nullElement %= modulus;
    cleanNulls();
  }

  public void add(int summand) {
    for (Map.Entry<Integer, MutableInteger> entry : values.entrySet()) {
      entry.getValue().value += summand;
    }
    nullElement += summand;
    cleanNulls();
  }

  public void cleanNulls() {
    for (Iterator<Map.Entry<Integer, MutableInteger>> it = values.entrySet().iterator(); it.hasNext();) {
      Map.Entry<Integer, MutableInteger> entry = it.next();
      if (entry.getValue().value == nullElement) {
        it.remove();
        firstComponent = Integer.MAX_VALUE;
      }
    }
  }

  public int componentCount() {
    return values.size();
  }

  private void assertValidComponent(int component) {
    if (component < 1 || component > dimension) {
      throw new IllegalArgumentException();
    }
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int column = 1; column <= dimension; column++) {
      if (column > 1) {
        builder.append("\t");
      }
      builder.append(get(column));
    }
    return builder.toString();
  }

  public int compareTo(SparseVector o) {
    int ourFirst = getFirstComponent();
    int theirFirst = o.getFirstComponent();
    return ourFirst < theirFirst ? -1 : (ourFirst == theirFirst ? compareComponentCount(o) : 1);
  }

  private int compareComponentCount(SparseVector o) {
    return componentCount() - o.componentCount();
  }

  private int getFirstComponent() {
    if (firstComponent == Integer.MAX_VALUE) {
      for (int component : values.keySet()) {
        if (component < firstComponent) {
          firstComponent = component;
        }
      }
    }
    return firstComponent;
  }

  public void forEachNonNullValue(CollectionUtils.Function<Integer, ? extends Object> function) {
    for (MutableInteger value : values.values()) {
      function.evaluate(value.value);
    }
  }
}
