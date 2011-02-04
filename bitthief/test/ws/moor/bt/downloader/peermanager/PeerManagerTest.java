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

package ws.moor.bt.downloader.peermanager;

import ws.moor.bt.stats.FakeRepository;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.tracker.TrackerResponse;
import ws.moor.bt.util.AdjustableTimeSource;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.ExtendedTestCase;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class PeerManagerTest extends ExtendedTestCase {

  private static int uniquePort = 7341;

  public void testTwoDistinctPeers() throws IOException {
    PeerManager peerManager = createPeerManager();
    Collection<TrackerResponse.PeerInfo> peers = new ArrayList<TrackerResponse.PeerInfo>();
    peers.add(getRandomPeerInfo());
    peers.add(getRandomPeerInfo());
    peerManager.addPeersFromTrackerResponse(peers);
    assertEquals(2, peerManager.distinctPeersSeen());
  }

  private PeerManager createPeerManager() {
    return new PeerManager(new FakeRepository());
  }

  public void testSamePeerTwice() throws IOException {
    PeerManager peerManager = createPeerManager();
    Collection<TrackerResponse.PeerInfo> peers = new ArrayList<TrackerResponse.PeerInfo>();
    peers.add(getRandomPeerInfo());
    peers.add(getRandomPeerInfo());
    peers.add(getRandomPeerInfo());
    peers.add(peers.iterator().next());
    peerManager.addPeersFromTrackerResponse(peers);
    assertEquals(3, peerManager.distinctPeersSeen());
  }

  public void testGetConnectCandidates() throws IOException, InterruptedException {
    AdjustableTimeSource timeSource = new AdjustableTimeSource();

    PeerManager peerManager = createPeerManager();
    peerManager.setTimeSource(timeSource);

    timeSource.increaseTime(1);
    Collection<TrackerResponse.PeerInfo> peers = new ArrayList<TrackerResponse.PeerInfo>();
    TrackerResponse.PeerInfo a = getRandomPeerInfo();
    peers.add(a);
    TrackerResponse.PeerInfo b = getRandomPeerInfo();
    peers.add(b);
    peerManager.addPeersFromTrackerResponse(peers);

    timeSource.increaseTime(1);
    TrackerResponse.PeerInfo c = getRandomPeerInfo();
    peers.add(c);
    TrackerResponse.PeerInfo d = getRandomPeerInfo();
    peers.add(d);
    peerManager.addPeersFromTrackerResponse(peers);

    assertEquals(4, peerManager.distinctPeersSeen());

    timeSource.increaseTime(1);
    List<InetSocketAddress> candidates = peerManager.getConnectCandidates(2);
    assertEquals(2, candidates.size());
    assertContains(a.getSocketAddress(), candidates);
    assertContains(b.getSocketAddress(), candidates);

    timeSource.increaseTime(1);
    candidates = peerManager.getConnectCandidates(2);
    assertEquals(2, candidates.size());
    assertContains(c.getSocketAddress(), candidates);
    assertContains(d.getSocketAddress(), candidates);

    timeSource.increaseTime(1);
    candidates = peerManager.getConnectCandidates(2);
    assertEquals(2, candidates.size());
    assertContains(a.getSocketAddress(), candidates);
    assertContains(b.getSocketAddress(), candidates);

    timeSource.increaseTime(1);
    candidates = peerManager.getConnectCandidates(4);
    assertEquals(4, candidates.size());
    assertContains(a.getSocketAddress(), candidates);
    assertContains(b.getSocketAddress(), candidates);
    assertContains(c.getSocketAddress(), candidates);
    assertContains(d.getSocketAddress(), candidates);

    candidates = peerManager.getConnectCandidates(8);
    assertEquals(4, candidates.size());
  }

  private TrackerResponse.PeerInfo getRandomPeerInfo() throws UnknownHostException {
    return new TrackerResponse.PeerInfo(
        PeerId.createRandomMainlineId(),
        InetAddress.getByAddress(ByteUtil.randomByteArray(4)),
        uniquePort++);
  }
}
