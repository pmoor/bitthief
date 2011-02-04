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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * TODO(pmoor): Javadoc
 */
public class CacheSet<E> extends AbstractSet<E> implements Set<E>  {

  private final Map<E, Object> map;

  private static final Object DUMMY_VALUE = new Object();

  public CacheSet(int entryLimit) {
    map = new CacheMap<E, Object>(entryLimit);
  }

  public int size() {
    return map.size();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public boolean contains(Object o) {
    return map.containsKey(o);
  }

  public Iterator<E> iterator() {
    return map.keySet().iterator();
  }

  public Object[] toArray() {
    return map.keySet().toArray();
  }

  public <T>T[] toArray(T[] a) {
    return map.keySet().toArray(a);
  }

  public boolean add(E o) {
    return map.put(o, DUMMY_VALUE) == null;
  }

  public boolean remove(Object o) {
    return map.remove(o) == DUMMY_VALUE;
  }

  public boolean containsAll(Collection<?> c) {
    return map.keySet().containsAll(c);
  }

  public void clear() {
    map.clear();
  }
}
