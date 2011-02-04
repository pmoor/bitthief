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

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SparseVectorTest extends TestCase {

  private Map<Integer, Integer> testVectors;
  private SparseVector vector;

  public void testGet() {
    for (Map.Entry<Integer, Integer> entry : testVectors.entrySet()) {
      assertEquals((int) entry.getValue(), vector.get(entry.getKey()));
    }
  }
  
  public void testMultiply() {
    vector.multiply(3);
    for (Map.Entry<Integer, Integer> entry : testVectors.entrySet()) {
      assertEquals(3 * entry.getValue(), vector.get(entry.getKey()));
    }
  }

  public void testAdd() {
    vector.add(8);
    for (Map.Entry<Integer, Integer> entry : testVectors.entrySet()) {
      assertEquals(8 + entry.getValue(), vector.get(entry.getKey()));
    }
  }

  public void testAddMultiply() {
    vector.add(8);
    vector.multiply(2);
    for (Map.Entry<Integer, Integer> entry : testVectors.entrySet()) {
      assertEquals(2 * (8 + entry.getValue()), vector.get(entry.getKey()));
    }
  }

  public void testModulo() {
    vector.modulo(4);
    for (Map.Entry<Integer, Integer> entry : testVectors.entrySet()) {
      assertEquals(entry.getValue() % 4, vector.get(entry.getKey()));
    }
  }

  public void testCopyConstructor() {
    vector.set(17, 33);
    SparseVector vectorTwo = new SparseVector(vector);
    vector.set(17, 34);
    assertEquals(33, vectorTwo.get(17));
  }

  public void setUp() {
    Random rnd = new Random();

    testVectors = new HashMap<Integer, Integer>();
    for (int i = 0; i < 16; i++) {
      int component = rnd.nextInt(32) + 1;
      int value = rnd.nextInt();
      testVectors.put(component, value);
    }
    testVectors.put(7, 0);

    vector = new SparseVector(32);
    for (Map.Entry<Integer, Integer> entry : testVectors.entrySet()) {
      vector.set(entry.getKey(), entry.getValue());
    }
  }
}
