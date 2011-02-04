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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class TimeoutSet<E> {

  private HashMap<E, Long> backingMap;
  private long timeout;
  private int size;

  private TimeSource timeSource = SystemTimeSource.INSTANCE;

  public TimeoutSet(int maxEntries, long timeout) {
    this.backingMap = new CacheMap<E, Long>(maxEntries);
    this.timeout = timeout;
    size = maxEntries;
  }

  public void add(E o) {
    add(o, timeout);
  }

  public boolean contains(E o) {
    Long expirationTime = backingMap.get(o);
    if (expirationTime == null) {
      return false;
    }
    return validTime(expirationTime);
  }

  private boolean validTime(long expirationTime) {
    return expirationTime > timeSource.getTime();
  }

  public void setTimeSource(TimeSource timeSource) {
    this.timeSource = timeSource;
  }

  public void remove(E o) {
    backingMap.remove(o);
  }

  public synchronized void add(E o, long timeout) {
    backingMap.put(o, timeSource.getTime() + timeout);
  }

  public int size() {
    return size;
  }

  public synchronized void resize(int newSize) {
    if (newSize == backingMap.size()) {
      return;
    } else if (newSize < backingMap.size()) {
      backingMap = copyToSmallerMap(newSize);
    } else {
      backingMap = copyToLargerMap(newSize);
    }
    size = newSize;
  }

  public void setTimeout(long newTimeout) {
    timeout = newTimeout;
  }

  private HashMap<E, Long> copyToLargerMap(int newSize) {
    HashMap<E, Long> newMap = new CacheMap<E, Long>(newSize);
    newMap.putAll(backingMap);
    return newMap;
  }

  private HashMap<E, Long> copyToSmallerMap(int newSize) {
    List<E> sorted = new ArrayList<E>(backingMap.keySet());
    Collections.sort(sorted, new Comparator<E>() {
      public int compare(E o1, E o2) {
        return backingMap.get(o2).compareTo(backingMap.get(o1));
      }
    });

    HashMap<E, Long> newMap = new CacheMap<E, Long>(newSize);
    for (E e : sorted.subList(0, newSize)) {
      newMap.put(e, backingMap.get(e));
    }
    return newMap;
  }
}
