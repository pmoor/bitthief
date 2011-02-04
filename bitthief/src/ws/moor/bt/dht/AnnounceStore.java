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

import ws.moor.bt.stats.CounterRepository;
import ws.moor.bt.torrent.Hash;
import ws.moor.bt.tracker.TrackerResponse;
import ws.moor.bt.util.SystemTimeSource;
import ws.moor.bt.util.TimeSource;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO(pmoor): Javadoc
 */
public class AnnounceStore {

  private final Map<Hash, Map<InetSocketAddress, Entry>> map =
      new HashMap<Hash, Map<InetSocketAddress, Entry>>();

  private final int maxHashes;
  private final int maxEntriesPerHash;
  private final CounterRepository counterRepository;

  private TimeSource timeSource = SystemTimeSource.INSTANCE;

  public AnnounceStore(int maxHashes, int maxEntriesPerHash, CounterRepository counterRepository) {
    this.maxHashes = maxHashes;
    this.maxEntriesPerHash = maxEntriesPerHash;
    this.counterRepository = counterRepository;
  }

  public void setTimeSource(TimeSource timeSource) {
    this.timeSource = timeSource;
  }

  public synchronized void announce(Hash infoHash, InetSocketAddress address) {
    if (map.containsKey(infoHash)) {
      addAnnounceToExistingHash(infoHash, address);
    } else {
      addAnnounceToNewHash(infoHash, address);
    }
  }

  public synchronized TrackerResponse.PeerInfo[] getPeers(Hash infoHash, int count) {
    Map<InetSocketAddress, Entry> entries = map.get(infoHash);
    if (entries == null) {
      return null;
    }
    List<Entry> peers = new ArrayList<Entry>(entries.values());
    Collections.shuffle(peers);
    peers = peers.subList(0, Math.min(count, peers.size()));
    TrackerResponse.PeerInfo[] result = new TrackerResponse.PeerInfo[peers.size()];
    for (int i = 0; i < peers.size(); i++) {
      result[i] = entryToPeerInfo(peers.get(i));
    }
    return result;
  }

  public String dump() {
    StringBuilder builder = new StringBuilder();
    Set<Hash> hashes;
    synchronized (this) {
      hashes = new HashSet<Hash>(map.keySet());
    }
    for (Hash hash : hashes) {
      builder.append(hash).append("\t");
      builder.append(map.get(hash).size());
      builder.append("\n");
    }
    return builder.toString();
  }

  private TrackerResponse.PeerInfo entryToPeerInfo(Entry entry) {
    return new TrackerResponse.PeerInfo(null, entry.address);
  }

  private void addAnnounceToExistingHash(Hash infoHash, InetSocketAddress address) {
    Map<InetSocketAddress, Entry> entries = this.map.get(infoHash);
    if (entries == null) {
      throw new IllegalStateException("entries should not be null now");
    }
    updateAnnounceInMap(entries, address);
  }

  private void addAnnounceToNewHash(Hash infoHash, InetSocketAddress address) {
    if (map.size() >= maxHashes) {
      removeNonImportantHashes();
    }
    Map<InetSocketAddress, Entry> entries = new HashMap<InetSocketAddress, Entry>();
    map.put(infoHash, entries);
    updateAnnounceInMap(entries, address);
  }

  private void updateAnnounceInMap(Map<InetSocketAddress, Entry> entries, InetSocketAddress address) {
    Entry entry = entries.get(address);
    if (entry == null) {
      addNewAnnounceInMap(entries, address);
    } else {
      entry.refresh();
    }
  }

  private void addNewAnnounceInMap(Map<InetSocketAddress, Entry> entries, InetSocketAddress address) {
    if (entries.size() >= maxEntriesPerHash) {
      removeNonImportantEntries(entries);
    }
    entries.put(address, new Entry(address));
  }

  private void removeNonImportantEntries(Map<InetSocketAddress, Entry> entries) {
    long oldestEntryTime = Long.MAX_VALUE;
    InetSocketAddress oldestEntry = null;
    while (entries.size() >= maxEntriesPerHash) {
      for (Map.Entry<InetSocketAddress, Entry> entry : entries.entrySet()) {
        long lastRefresh = entry.getValue().lastRefresh;
        if (lastRefresh < oldestEntryTime) {
          oldestEntry = entry.getKey();
          oldestEntryTime = lastRefresh;
        }
      }
      entries.remove(oldestEntry);
    }
  }

  private void removeNonImportantHashes() {
    int smallestHashSize = Integer.MAX_VALUE;
    Hash smallestHash = null;
    while (map.size() >= maxHashes) {
      for (Map.Entry<Hash, Map<InetSocketAddress, Entry>> entry : map.entrySet()) {
        int size = entry.getValue().size();
        if (size < smallestHashSize) {
          smallestHash = entry.getKey();
          smallestHashSize = size;
        }
      }
      map.remove(smallestHash);
    }
  }

  public void maintain() {
    List<Map<InetSocketAddress, Entry>> hashes;
    synchronized (this) {
      hashes = new ArrayList<Map<InetSocketAddress, Entry>>(map.values());
    }
    int peers = 0;
    for (Map<InetSocketAddress, Entry> entry : hashes) {
      peers += entry.size();
    }
    counterRepository.getCounter("dht.store.peers").set(peers);
    counterRepository.getCounter("dht.store.hashes").set(map.size());
  }

  private class Entry {
    private final InetSocketAddress address;
    private final long firstSeen;
    private long lastRefresh;

    public Entry(InetSocketAddress address) {
      this.address = address;
      firstSeen = timeSource.getTime();
      lastRefresh = firstSeen;
    }

    public void refresh() {
      lastRefresh = timeSource.getTime();
    }
  }
}
