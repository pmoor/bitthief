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

package ws.moor.bt.stats;

import ws.moor.bt.stats.recorder.Recorder;
import ws.moor.bt.util.ArrayUtil;
import ws.moor.bt.util.SystemTimeSource;
import ws.moor.bt.util.TimeSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
class CounterImpl implements Counter {

  private long value;
  private final String name;
  private final TimeSource timeSource;

  private Counter[] parents =
      new Counter[0];

  private Recorder[] recorders =
      new Recorder[0];

  CounterImpl(long value, String name, TimeSource timeSource) {
    this.value = value;
    this.name = name;
    this.timeSource = timeSource;
  }

  CounterImpl(long value, String name) {
    this(value, name, SystemTimeSource.INSTANCE);
  }

  public synchronized void set(long value) {
    increaseParentCounters(value - this.value);
    this.value = value;
    record(timeSource.getTime(), this.value);
  }

  public synchronized long increase(long amount) {
    set(value + amount);
    return value;
  }

  public synchronized long decrease(long amount) {
    return increase(-amount);
  }

  public String getName() {
    return name;
  }

  public long get() {
    return value;
  }

  synchronized void addRecorder(Recorder recorder) {
    recorders = ArrayUtil.append(recorders, recorder);
    recorder.record(timeSource.getTime(), value);
  }

  synchronized void addParent(Counter counter) {
    parents = ArrayUtil.append(parents, counter);
    counter.increase(value);
  }

  private void increaseParentCounters(long amount) {
    if (amount != 0) {
      for (Counter parent : parents) {
        parent.increase(amount);
      }
    }
  }

  private synchronized void record(long time, long value) {
    for (Recorder recorder : recorders) {
      recorder.record(time, value);
    }
  }

  List<Recorder> getRecorders() {
    return Collections.unmodifiableList(Arrays.asList(recorders));
  }
}
