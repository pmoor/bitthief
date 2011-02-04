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

import ws.moor.bt.stats.Counter;
import ws.moor.bt.stats.CounterRepository;
import ws.moor.bt.stats.RealCounterRepository;
import ws.moor.bt.util.AdjustableTimeSource;
import ws.moor.bt.util.SystemTimeSource;
import ws.moor.bt.util.TimeSource;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

/**
 * TODO(pmoor): Javadoc
 */
public class Agenda {

  private final AdjustableTimeSource timeSource =
      new AdjustableTimeSource();

  private final TimeSource realTimeSource =
      SystemTimeSource.INSTANCE;

  private Queue<Entry> agenda = new PriorityQueue<Entry>(1024);

  private final CounterRepository repository;
  private final Counter eventCounter;
  private final Counter queueSizeCounter;
  private final Counter timeCounter;

  private static final Random rnd = new Random();

  public Agenda() {
    repository = createRepository();
    queueSizeCounter = repository.getCounter("agenda.queuesize");
    eventCounter = repository.getCounter("agenda.events");
    timeCounter = repository.getCounter("agenda.realtime");
  }

  private CounterRepository createRepository() {
    RealCounterRepository repository = RealCounterRepository.fromResource("stats.properties");
    repository.setFileForWriting("/tmp/Simulator-Stats.csv");
    repository.setTimeSource(timeSource);
    return repository;
  }

  public synchronized void schedule(Event event, long time) {
    agenda.offer(new Entry(time, event));
  }

  public synchronized void scheduleDelta(Event event, long timeDelta) {
    agenda.offer(new Entry(timeSource.getTime() + timeDelta, event));
  }

  public synchronized void scheduleRandomized(Event event, long timeDelta, long maxDeviation) {
    long time = timeDelta + (long) (maxDeviation * rnd.nextGaussian() / 2);
    time = Math.max(timeDelta - maxDeviation, Math.min(timeDelta + maxDeviation, time));
    scheduleDelta(event, time);
  }

  public TimeSource getTimeSource() {
    return timeSource;
  }

  public void run() {
    while (!agenda.isEmpty()) {
      Entry entry = agenda.poll();
      timeSource.setTime(entry.executionTime);

      Event event = entry.event;
      if (event.doRun()) {
        try {
          event.execute();
        } catch (Exception e) {
          // do something
        }
      }

      long rescheduleInterval = event.getRescheduleInterval();
      if (rescheduleInterval > 0) {
        entry.advance(rescheduleInterval);
        agenda.offer(entry);
      }

      updateStats();
    }
  }

  private void updateStats() {
    queueSizeCounter.set(agenda.size());
    eventCounter.increase(1);
    timeCounter.set(realTimeSource.getTime());
  }

  public CounterRepository getRepository() {
    return repository;
  }

  private static class Entry implements Comparable<Entry> {

    private long executionTime;
    private final Event event;

    public Entry(long executionTime, Event event) {
      this.executionTime = executionTime;
      this.event = event;
    }

    public int compareTo(Entry other) {
      return executionTime < other.executionTime ?
          -1 : (executionTime == other.executionTime ? 0 : 1);
    }

    public void advance(long interval) {
      executionTime += interval;
    }
  }
}
