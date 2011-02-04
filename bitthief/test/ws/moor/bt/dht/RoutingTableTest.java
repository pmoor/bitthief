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

import ws.moor.bt.stats.FakeRepository;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.tracker.TrackerResponse;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.ExtendedTestCase;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class RoutingTableTest extends ExtendedTestCase {

  private PeerId ourId = PeerId.createRandom();
  private Pinger pinger = new Pinger() {
    public void ping(InetSocketAddress address) {
      fail("should not be called");
    }
  };

  public void testCloseNodes() throws Exception {
    RoutingTable table = new RoutingTable(ourId, pinger, new FakeRepository());
    table.maintain();

    List<TrackerResponse.PeerInfo> peers = createAWholeBunchOfPeers(500);
    for (TrackerResponse.PeerInfo info : peers) {
      table.addNode(info);
    }

    assertTrue(230 < table.size());

    Collections.sort(peers, distanceComparator(ourId));
    assertTrue(ourId.distance(peers.get(0).getPeerId())
        > ourId.distance(peers.get(peers.size() - 1).getPeerId()));

    TrackerResponse.PeerInfo[] closeNodes = table.getNodesCloseTo(ourId, 40);
    assertEquals(40, closeNodes.length);

    for (TrackerResponse.PeerInfo info : closeNodes) {
      assertContains(info, peers.subList(0, 50));
    }
  }

  private Comparator<TrackerResponse.PeerInfo> distanceComparator(final PeerId id) {
    return new Comparator<TrackerResponse.PeerInfo>() {
      public int compare(TrackerResponse.PeerInfo o1, TrackerResponse.PeerInfo o2) {
        return id.distance(o2.getPeerId()) - id.distance(o1.getPeerId());
      }
    };
  }

  private List<TrackerResponse.PeerInfo> createAWholeBunchOfPeers(int count) throws Exception {
    List<TrackerResponse.PeerInfo> result = new ArrayList<TrackerResponse.PeerInfo>(count);
    for (int i = 0; i < count; i++) {
      result.add(createRandomPeerInfo());
    }
    return result;
  }

  private TrackerResponse.PeerInfo createRandomPeerInfo() throws Exception {
    PeerId peerId = PeerId.createRandom();
    InetAddress address = InetAddress.getByAddress(ByteUtil.randomByteArray(4));
    int port = rnd.nextInt(60000) + 4000;
    return new TrackerResponse.PeerInfo(peerId, address, port);
  }
}
