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

package ws.moor.bt.util;

import java.util.Arrays;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class CollectionUtilsTest extends ExtendedTestCase {

  public void testMap() {
    List<String> src = Arrays.asList("1", "2", "3", "4");

    List<Integer> result = CollectionUtils.map(src, new CollectionUtils.Function<String, Integer>() {
      public Integer evaluate(String source) {
        return Integer.parseInt(source);
      }
    });

    assertEquals(Arrays.asList(1, 2, 3, 4), result);
  }

  public void testMapNullRemoves() {
    List<String> src = Arrays.asList("1", "2", "3", "4");

    List<Integer> result =
        CollectionUtils.mapNullRemoves(src, new CollectionUtils.Function<String, Integer>() {
      public Integer evaluate(String source) {
        int integer = Integer.parseInt(source);
        return integer % 2 == 0 ? integer : null;
      }
    });

    assertEquals(Arrays.asList(2, 4), result);
  }

  public void testToDoubleArray() {
    List<Double> list = Arrays.asList(1d, 2d, 3d, 4d);
    double[] result = CollectionUtils.toArray(list);
    assertArrayEquals(new double[] {1, 2, 3, 4}, result);
  }

  public void testPickRandom() {
    List<Integer> list = Arrays.asList(15, 18, 22, 23, -5, 88);
    for (int i = 0; i < 10; i++) {
      assertContains(CollectionUtils.pickRandom(list), list);
    }
  }
}
