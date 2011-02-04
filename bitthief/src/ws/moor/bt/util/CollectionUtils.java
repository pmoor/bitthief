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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * TODO(pmoor): Javadoc
 */
public class CollectionUtils {

  private static final Random rnd = new Random();

  public static <R, S> List<R> map(Iterable<S> list, Function<S, R> function) {
    List<R> result = new ArrayList<R>();
    for (S s : list) {
      result.add(function.evaluate(s));
    }
    return result;
  }

  public static <R, S> List<R> mapNullRemoves(Iterable<S> list, Function<S, R> function) {
    List<R> result = new ArrayList<R>();
    for (S s : list) {
      R r = function.evaluate(s);
      if (r != null) {
        result.add(r);
      }
    }
    return result;
  }

  public static double[] toArray(Collection<Double> collection) {
    double[] result = new double[collection.size()];
    int i = 0;
    for (double value : collection) {
      result[i++] = value;
    }
    return result;
  }

  public static <T> T pickRandom(List<T> collection) {
    int index = rnd.nextInt(collection.size());
    return collection.get(index);
  }

  public interface Function<S, R> {
    public R evaluate(S source);
  }
}
