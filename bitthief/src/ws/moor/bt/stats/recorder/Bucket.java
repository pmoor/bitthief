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

package ws.moor.bt.stats.recorder;

/**
 * TODO(pmoor): Javadoc
 */
public class Bucket {

  private final long timeInterval;

  private long currentBucketStartTime;
  private int currentBucket;
  private int currentBucketPendingValues;
  private long latestValue;

  private long oldestValidTime;

  private long[] buckets;

  public static final long INVALID_VALUE = Long.MAX_VALUE;
  private boolean wrapped = false;

  public Bucket(int bucketCount, long timeInterval, long startTime) {
    this.timeInterval = timeInterval;
    buckets = new long[bucketCount];

    currentBucketStartTime = startTime;
    currentBucket = 0;
    currentBucketPendingValues = 0;
    oldestValidTime = currentBucketStartTime;
  }

  public synchronized void addValue(long time, long value) {
    advanceCurrentBucket(time);
    if (currentBucketPendingValues == 0) {
      buckets[currentBucket] = 0;
    }
    buckets[currentBucket] += value;
    currentBucketPendingValues++;
    latestValue = value;
  }

  public long get(long time) {
    int bucket = determineBucketForTime(time);
    if (bucket == Integer.MAX_VALUE) {
      return getLatestValue();
    } else if (bucket == Integer.MIN_VALUE) {
      return get(getOldestTime());
    } else if (bucket == currentBucket && currentBucketPendingValues > 0) {
      return buckets[currentBucket] / currentBucketPendingValues;
    } else {
      return buckets[bucket];
    }
  }

  public long getOldestTime() {
    return oldestValidTime;
  }

  public long getNewestTime() {
    return currentBucketStartTime + timeInterval - 1;
  }

  public long getLatestValue() {
    return latestValue;
  }

  public double getDerivative(long time) {
    if (time < getOldestTime() || time > getNewestTime()) {
      return 0.0;
    }

    long earlyTime = Math.max(time - timeInterval, getOldestTime());
    long earlyValue = get(earlyTime);
    long lateValue = get(earlyTime + timeInterval);
    return (double) (lateValue - earlyValue) / timeInterval;
  }

  public long getInterval() {
    return timeInterval;
  }

  private int determineBucketForTime(long time) {
    if (time > getNewestTime()) {
      return Integer.MAX_VALUE;
    }
    if (time < getOldestTime()) {
      return Integer.MIN_VALUE;
    }
    int bucketsFromCurrent = (int) ((currentBucketStartTime + timeInterval - 1 - time) / timeInterval);
    return (currentBucket + buckets.length - bucketsFromCurrent) % buckets.length;
  }

  private void advanceCurrentBucket(long time) {
    if (time - currentBucketStartTime < timeInterval) {
      return;
    }

    // finish current bucket
    if (currentBucketPendingValues > 0) {
      buckets[currentBucket] /= currentBucketPendingValues;
    }

    // wipe out all intermediate buckets
    while (time - currentBucketStartTime >= timeInterval) {
      currentBucket = (currentBucket + 1) % buckets.length;
      if (currentBucket == 0) {
        wrapped = true;
      }
      currentBucketStartTime += timeInterval;
      buckets[currentBucket] = latestValue;
    }
    currentBucketPendingValues = 0;

    if (wrapped) {
      oldestValidTime = currentBucketStartTime - timeInterval * (buckets.length - 1);
    }
  }
}
