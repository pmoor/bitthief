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

package ws.moor.bt.network;

import org.apache.log4j.Logger;
import ws.moor.bt.stats.CounterRepository;
import ws.moor.bt.util.LoggingUtil;
import ws.moor.bt.util.SystemTimeSource;
import ws.moor.bt.util.TimeSource;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * TODO(pmoor): Javadoc
 */
public class CostThrottler {

  private final int maxCosts;
  private final long interval;

  private volatile long busyUntil = 0;

  private final LinkedHashSet<WaitingJob> waitingJobs = new LinkedHashSet<WaitingJob>();

  private final Runner runner = new Runner();

  private final CounterRepository counterRepository;

  private TimeSource timeSource = SystemTimeSource.INSTANCE;

  private static final Logger logger = LoggingUtil.getLogger(CostThrottler.class);

  public CostThrottler(int maxCosts, long interval, CounterRepository counterRepository) {
    this.interval = interval;
    this.maxCosts = maxCosts;
    this.counterRepository = counterRepository;
  }

  public void start() {
    runner.start();
  }

  public void stop() {
    runner.stop();
  }

  public void submitJob(int costs, Runnable job) {
    WaitingJob waitingJob = new WaitingJob(job, costs);
    synchronized (waitingJobs) {
      if (waitingJobs.contains(waitingJob)) {
        return;
      }
      waitingJobs.add(waitingJob);
      updateWaitingJobsStatistics();
    }
    synchronized (runner) {
      runner.notify();
    }
  }

  public void removeJob(Runnable job) {
    WaitingJob waitingJob = new WaitingJob(job, 0);
    synchronized (waitingJobs) {
      waitingJobs.remove(waitingJob);
      updateWaitingJobsStatistics();
    }
  }

  private void processNextJob() {
    if (noPendingJobs()) {
      return;
    }

    WaitingJob job = null;
    synchronized (waitingJobs) {
      long now = timeSource.getTime();
      if (busyUntil <= now + 10) {
        job = removeFirstWaitingJob();
        long busyPeriod = calculateBusyPeriod(job);
        busyUntil = Math.max(busyUntil + busyPeriod, now - 50 + busyPeriod);
      }
    }
    runJob(job);
  }

  private void runJob(WaitingJob job) {
    if (job != null) {
      try {
        job.job.run();
      } catch (Exception e) {
        logger.warn("exception during running of job", e);
      }
    }
  }

  private WaitingJob removeFirstWaitingJob() {
    synchronized (waitingJobs) {
      Iterator<WaitingJob> iterator = waitingJobs.iterator();
      WaitingJob job = null;
      if (iterator.hasNext()) {
        job = iterator.next();
        iterator.remove();
      }
      updateWaitingJobsStatistics();
      return job;
    }
  }

  private long calculateBusyPeriod(WaitingJob job) {
    return interval * job.costs / maxCosts;
  }

  private boolean noPendingJobs() {
    return waitingJobs.isEmpty();
  }

  private void updateWaitingJobsStatistics() {
    counterRepository.getCounter("torrent.throttler.waitingjobs").set(waitingJobs.size());
  }

  private class Runner implements Runnable {
    private Thread thread;

    private boolean stop = true;

    private static final int DEFAULT_WAIT_TIMEOUT = 5000;

    public void start() {
      stop = false;
      thread = new Thread(this, "CostThrottler");
      thread.start();
    }

    public void stop() {
      stop = true;
    }

    public void run() {
      while (!stop) {
        processNextJob();
        sleep();
      }
    }

    private synchronized void sleep() {
      try {
        long timeToSleep = busyUntil - timeSource.getTime();
        if (timeToSleep > 10) {
          runner.wait(timeToSleep);
        } else if (noPendingJobs()) {
          runner.wait(DEFAULT_WAIT_TIMEOUT);
        }
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }

  private static class WaitingJob {
    private final Runnable job;
    private final int costs;

    public WaitingJob(Runnable job, int costs) {
      this.job = job;
      this.costs = costs;
    }

    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      return job == ((WaitingJob) o).job;
    }

    public int hashCode() {
      return job.hashCode();
    }
  }
}
