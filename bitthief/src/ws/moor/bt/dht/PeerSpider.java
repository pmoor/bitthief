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

import org.apache.log4j.Logger;
import ws.moor.bt.stats.CounterRepository;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.tracker.TrackerResponse;
import ws.moor.bt.util.CacheSet;
import ws.moor.bt.util.LoggingUtil;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PeerSpider {

  private final PeerId closeToOurId;
  private final NodeFinder finder;
  private final CounterRepository counterRepository;

  private final CacheSet<TrackerResponse.PeerInfo> peers =
      new CacheSet<TrackerResponse.PeerInfo>(2048);
  private final CacheSet<InetAddress> recentlyQueriedAddresses =
      new CacheSet<InetAddress>(16384);

  private static final Logger logger = LoggingUtil.getLogger(PeerSpider.class);

  public PeerSpider(PeerId ourId, NodeFinder finder, CounterRepository counterRepository) {
    this.finder = finder;
    this.counterRepository = counterRepository;
    closeToOurId = ourId.flipLastBit();
  }

  public synchronized void heardAbout(TrackerResponse.PeerInfo peer) {
    logger.trace("heard about " + peer);
    if (!justSentAFindNodeTo(peer)) {
      peers.add(peer);
      counterRepository.getCounter("dht.possiblepeers").set(peers.size());
    }
  }

  private boolean justSentAFindNodeTo(TrackerResponse.PeerInfo peer) {
    return recentlyQueriedAddresses.contains(peer.getAddress());
  }

  public void maintain() {
    logger.debug("maintenance");
    int peersToGet = 16;
    for (TrackerResponse.PeerInfo info : getACouplePeers(peersToGet)) {
      finder.findNode(info.getSocketAddress(), closeToOurId);
      recentlyQueriedAddresses.add(info.getAddress());
    }
  }

  private synchronized Set<TrackerResponse.PeerInfo> getACouplePeers(int count) {
    Set<TrackerResponse.PeerInfo> set = new HashSet<TrackerResponse.PeerInfo>(count);
    for (Iterator<TrackerResponse.PeerInfo> it = peers.iterator(); it.hasNext() && set.size() < count;) {
      TrackerResponse.PeerInfo info = it.next();
      it.remove();
      if (!justSentAFindNodeTo(info)) {
        set.add(info);
      }
    }
    counterRepository.getCounter("dht.possiblepeers").set(peers.size());
    return set;
  }
}
