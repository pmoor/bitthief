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

package ws.moor.bt.dht;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import ws.moor.bt.stats.CounterRepository;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.tracker.TrackerResponse;
import ws.moor.bt.util.LoggingUtil;
import ws.moor.bt.util.SystemTimeSource;
import ws.moor.bt.util.TimeSource;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO(pmoor): Javadoc
 */
public class RoutingTable {

  private final int peersPerBucket = 32;

  private Bucket[] buckets =
      new Bucket[] {
          new Bucket(Integer.MIN_VALUE, -1, peersPerBucket),
          new Bucket(0, 1000, peersPerBucket),
          new Bucket(1001, Integer.MAX_VALUE, peersPerBucket)};

  private final PeerId ourId;

  private final TimeSource timeSource = SystemTimeSource.INSTANCE;

  private final Pinger pinger;
  private final CounterRepository counterRepository;

  private static final Logger logger =
      LoggingUtil.getLogger(RoutingTable.class);

  public RoutingTable(PeerId ourId, Pinger pinger, CounterRepository counterRepository) {
    this.ourId = ourId;
    this.pinger = pinger;
    this.counterRepository = counterRepository;
  }

  public void addNode(TrackerResponse.PeerInfo node) {
    if (ourId.equals(node.getPeerId())) {
      return;
    }
    internalAdd(node);
  }

  private synchronized void internalAdd(TrackerResponse.PeerInfo node) {
    int value = determineValue(node);
    int bucketIndex = getBucketIndexForValue(value);
    if (buckets[bucketIndex].canAddMore() || !buckets[bucketIndex].canSplit()) {
      buckets[bucketIndex].add(node);
    } else {
      splitBucket(bucketIndex);
      internalAdd(node);
    }
  }

  private int determineValue(TrackerResponse.PeerInfo node) {
    return ourId.distance(node.getPeerId());
  }

  private synchronized void splitBucket(int bucketIndex) {
    Bucket currentBucket = buckets[bucketIndex];
    int splitValue = (int) (((long) currentBucket.minValue + (long) currentBucket.maxValue) / 2);
    Bucket lowerBucket = new Bucket(currentBucket.minValue, splitValue, peersPerBucket);
    Bucket upperBucket = new Bucket(splitValue + 1, currentBucket.maxValue, peersPerBucket);

    Bucket[] newBuckets = new Bucket[buckets.length + 1];
    System.arraycopy(buckets, 0, newBuckets, 0, bucketIndex);
    newBuckets[bucketIndex] = lowerBucket;
    newBuckets[bucketIndex + 1] = upperBucket;
    System.arraycopy(buckets, bucketIndex + 1, newBuckets, bucketIndex + 2, buckets.length - bucketIndex - 1);
    buckets = newBuckets;

    for (TrackerResponse.PeerInfo node : currentBucket.getEntries()) {
      internalAdd(node);
    }
  }

  private int getBucketIndexForValue(int value) {
    int currentBucket = buckets.length / 2;
    while (!buckets[currentBucket].contains(value)) {
      if (buckets[currentBucket].tooLarge(value)) {
        currentBucket++;
      } else {
        currentBucket--;
      }
    }
    return currentBucket;
  }

  public String averageDistancesPerBucket() {
    StringBuilder builder = new StringBuilder();
    for (Bucket bucket : buckets) {
      builder.append(bucket.toString()).append(": ");
      int distanceSum = 0;
      Collection<TrackerResponse.PeerInfo> entries = bucket.getEntries();
      for (TrackerResponse.PeerInfo node : entries) {
        distanceSum += ourId.distance(node.getPeerId());
      }
      double average = (double) distanceSum / entries.size();
      builder.append(average).append("\n");
    }
    return builder.toString();
  }

  public String dump() {
    StringBuilder builder = new StringBuilder();
    builder.append(Hex.encodeHex(ourId.getBytes()));
    builder.append("\n");
    for (Bucket bucket : buckets) {
      builder.append(bucket.dump());
    }
    return builder.toString();
  }

  public synchronized TrackerResponse.PeerInfo[] getNodesCloseTo(final PeerId id, int count) {
    int distance = ourId.distance(id);
    int bucket = getBucketIndexForValue(distance);
    List<TrackerResponse.PeerInfo> result = new ArrayList<TrackerResponse.PeerInfo>();
    int delta = 0;
    while (result.size() < count) {
      if (bucket + delta < buckets.length) {
        result.addAll(buckets[bucket + delta].getEntries());
      }
      if (bucket - delta >= 0 && delta != 0) {
        result.addAll(buckets[bucket - delta].getEntries());
      }
      delta++;
      if (bucket + delta >= buckets.length && bucket - delta < 0) {
        break;
      }
    }

    Collections.sort(result, new Comparator<TrackerResponse.PeerInfo>() {
      public int compare(TrackerResponse.PeerInfo o1, TrackerResponse.PeerInfo o2) {
        int a = id.distance(o1.getPeerId());
        int b = id.distance(o2.getPeerId());
        return b - a;
      }
    });

    result = result.subList(0, Math.min(count, result.size()));

    return result.toArray(new TrackerResponse.PeerInfo[result.size()]);
  }

  public void maintain() {
    Bucket[] buckets = getBucketsCopy();
    for (Bucket bucket : buckets) {
      bucket.maintain();
    }
    updateStatistics(buckets);
  }

  private void updateStatistics(Bucket[] buckets) {
    int activePeers = 0;
    int waitingPeers = 0;
    for (Bucket bucket : buckets) {
      activePeers += bucket.getActivePeerCount();
      waitingPeers += bucket.getWaitingPeerCount();
    }
    counterRepository.getCounter("dht.routing.peers.primary").set(activePeers);
    counterRepository.getCounter("dht.routing.peers.secondary").set(waitingPeers);
    counterRepository.getCounter("dht.routing.buckets").set(buckets.length);
  }

  private synchronized Bucket[] getBucketsCopy() {
    return buckets.clone();
  }

  public synchronized int size() {
    int sum = 0;
    for (Bucket bucket : buckets) {
      sum += bucket.getActivePeerCount();
    }
    return sum;
  }

  private class Bucket {
    private final int minValue;
    private final int maxValue;
    private final int capacity;
    private int waitingCapacity;

    private final Map<InetSocketAddress, Entry> entries =
        new HashMap<InetSocketAddress, Entry>();
    private final Map<InetSocketAddress, Entry> waitingEntries =
        new HashMap<InetSocketAddress, Entry>();

    public Bucket(int minValue, int maxValue, int capacity) {
      if (minValue > maxValue) {
        throw new IllegalArgumentException();
      } else if (capacity <= 0) {
        throw new IllegalArgumentException();
      }
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.capacity = capacity;
      waitingCapacity = capacity / 2;
    }

    public synchronized void add(TrackerResponse.PeerInfo e) {
      InetSocketAddress address = e.getSocketAddress();
      if (entries.containsKey(address)) {
        RoutingTable.Bucket.Entry entry = entries.get(address);
        entry.update(e);
      } else if (canAddMore()) {
        entries.put(address, new Entry(e));
      } else {
        addEntryToWaitingList(e);
      }
    }

    private void addEntryToWaitingList(TrackerResponse.PeerInfo peerInfo) {
      InetSocketAddress address = peerInfo.getSocketAddress();
      Entry entry = waitingEntries.get(address);
      if (entry == null) {
        if (waitingEntries.size() < waitingCapacity) {
          entry = new Entry(peerInfo);
          waitingEntries.put(address, entry);
        }
      } else {
        entry.update(peerInfo);
      }
    }

    public boolean contains(int value) {
      return value >= minValue && value <= maxValue;
    }

    public boolean tooLarge(int value) {
      return value > maxValue;
    }

    public boolean canSplit() {
      return maxValue > minValue;
    }

    public boolean canAddMore() {
      return entries.size() < capacity;
    }

    public synchronized Collection<TrackerResponse.PeerInfo> getEntries() {
      List<TrackerResponse.PeerInfo> result =
          new ArrayList<TrackerResponse.PeerInfo>(entries.size());
      for (Entry entry : entries.values()) {
        result.add(entry.info);
      }
      return result;
    }

    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(minValue).append("-").append(maxValue);
      builder.append(" (");
      builder.append(entries.size()).append("/");
      builder.append(capacity).append(")");
      return builder.toString();
    }

    public String dump() {
      StringBuilder builder = new StringBuilder();
      for (Entry entry : entries.values()) {
        builder.append(Hex.encodeHex(entry.info.getPeerId().getBytes()));
        builder.append("\t");
        builder.append(entry.info.getAddress().getHostAddress());
        builder.append("\t");
        builder.append(entry.info.getPort());
        builder.append("\t");
        builder.append(this.toString());
        builder.append("\t");
        builder.append(ourId.distance(entry.info.getPeerId()));
        builder.append("\t");
        builder.append(new Date(entry.firstAdded));
        builder.append("\t");
        builder.append(new Date(entry.lastUpdate));
        builder.append("\t");
        builder.append(new Date(entry.lastPinged));
        builder.append("\t");
        builder.append(entry.pingAttempts);
        builder.append("\t");
        builder.append(entry.totalPings);
        builder.append("\n");
      }
      return builder.toString();
    }

    public synchronized void maintain() {
      int count = removeDeadEntries(entries);
      counterRepository.getCounter("dht.routing.remove.primary").increase(count);
      count = removeDeadEntries(waitingEntries);
      counterRepository.getCounter("dht.routing.remove.secondary").increase(count);
      count = transferEntries();
      counterRepository.getCounter("dht.routing.transfer").increase(count);
    }

    private int transferEntries() {
      int entriesTransfered = 0;
      Collection<Entry> copyOfEntries = new ArrayList<Entry>(waitingEntries.values());
      for (Entry entry : copyOfEntries) {
        if (entries.size() >= capacity) {
          break;
        }
        entries.put(entry.info.getSocketAddress(), entry);
        waitingEntries.remove(entry.info.getSocketAddress());
        entriesTransfered++;
      }
      return entriesTransfered;
    }

    private int removeDeadEntries(Map<InetSocketAddress, Entry> entries) {
      int entriesRemoved = 0;
      Collection<Entry> originalEntries =
          new ArrayList<Entry>(entries.values());
      for (Entry entry : originalEntries) {
        entry.maintain();
        if (entry.isDead()) {
          logger.info("removing dead entry: " + entry.info +
              "\nnot seen since " + new Date(entry.lastUpdate));
          entries.remove(entry.info.getSocketAddress());
          entriesRemoved++;
        }
      }
      return entriesRemoved;
    }

    public int getActivePeerCount() {
      return entries.size();
    }

    public int getWaitingPeerCount() {
      return waitingEntries.size();
    }

    private class Entry {
      private TrackerResponse.PeerInfo info;
      private final long firstAdded;
      private long lastUpdate;
      private long lastPinged;
      private int pingAttempts;
      private int totalPings;

      private static final long TIMEOUT = 15 * 60 * 1000;
      private static final long PING_TIMEOUT = 60 * 1000;
      private static final int MAX_PING_ATTEMPTS = 5;

      public Entry(TrackerResponse.PeerInfo info) {
        this.info = info;
        firstAdded = timeSource.getTime();
        lastUpdate = firstAdded;
      }

      public void update(TrackerResponse.PeerInfo info) {
        if (!this.info.getSocketAddress().equals(info.getSocketAddress())) {
          throw new IllegalArgumentException("socket address differs");
        }
        if (!this.info.getPeerId().equals(info.getPeerId())) {
          this.info = info;
        }
        lastUpdate = timeSource.getTime();
        lastPinged = 0;
        pingAttempts = 0;
      }

      public void maintain() {
        long now = timeSource.getTime();
        if (lastUpdate + TIMEOUT > now) {
          return;
        }
        if (lastPinged + PING_TIMEOUT > now) {
          return;
        }
        if (pingAttempts < MAX_PING_ATTEMPTS) {
          pingAttempts++;
          totalPings++;
          lastPinged = now;
          pinger.ping(info.getSocketAddress());
        }
      }

      public boolean isDead() {
        return lastPinged + PING_TIMEOUT < timeSource.getTime() && pingAttempts >= MAX_PING_ATTEMPTS;
      }
    }
  }
}
