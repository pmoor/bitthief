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
import ws.moor.bt.torrent.Hash;
import ws.moor.bt.torrent.HashTest;
import ws.moor.bt.tracker.TrackerResponse;
import ws.moor.bt.util.AdjustableTimeSource;
import ws.moor.bt.util.CollectionUtils;
import ws.moor.bt.util.ExtendedTestCase;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class AnnounceStoreTest extends ExtendedTestCase {

  private AdjustableTimeSource timeSource = new AdjustableTimeSource();

  public void testEmpty() {
    AnnounceStore announceStore = new AnnounceStore(4, 2, new FakeRepository());
    announceStore.setTimeSource(timeSource);

    Hash infoHash = HashTest.randomHash();

    TrackerResponse.PeerInfo[] peers =
        announceStore.getPeers(infoHash, 8);
    assertNull(peers);
  }

  public void testAddSome() {
    AnnounceStore announceStore = new AnnounceStore(4, 2, new FakeRepository());
    announceStore.setTimeSource(timeSource);

    Hash infoHash = HashTest.randomHash();

    InetSocketAddress address = randomInetSocketAddress();
    announceStore.announce(infoHash, address);

    TrackerResponse.PeerInfo[] peers =
        announceStore.getPeers(infoHash, 8);
    assertNotNull(peers);
    assertEquals(1, peers.length);
    assertEquals(address, peers[0].getSocketAddress());
  }

  public void testToManyDifferentHashes() {
    AnnounceStore announceStore = new AnnounceStore(4, 6, new FakeRepository());
    announceStore.setTimeSource(timeSource);

    Hash infoHashA = HashTest.randomHash();
    announceStore.announce(infoHashA, randomInetSocketAddress());
    announceStore.announce(infoHashA, randomInetSocketAddress());
    announceStore.announce(infoHashA, randomInetSocketAddress());
    announceStore.announce(infoHashA, randomInetSocketAddress());

    Hash infoHashB = HashTest.randomHash();
    announceStore.announce(infoHashB, randomInetSocketAddress());

    Hash infoHashC = HashTest.randomHash();
    announceStore.announce(infoHashC, randomInetSocketAddress());
    announceStore.announce(infoHashC, randomInetSocketAddress());

    Hash infoHashD = HashTest.randomHash();
    announceStore.announce(infoHashD, randomInetSocketAddress());

    assertEquals(4, announceStore.getPeers(infoHashA, 8).length);
    assertEquals(1, announceStore.getPeers(infoHashB, 8).length);
    assertEquals(2, announceStore.getPeers(infoHashC, 8).length);
    assertEquals(1, announceStore.getPeers(infoHashD, 8).length);

    Hash infoHashE = HashTest.randomHash();
    announceStore.announce(infoHashE, randomInetSocketAddress());
    announceStore.announce(infoHashE, randomInetSocketAddress());

    announceStore.announce(HashTest.randomHash(), randomInetSocketAddress());

    assertNull(announceStore.getPeers(infoHashB, 8));
    assertNull(announceStore.getPeers(infoHashD, 8));
  }

  public void testToManyEntries() {
    AnnounceStore announceStore = new AnnounceStore(1, 4, new FakeRepository());
    announceStore.setTimeSource(timeSource);

    Hash infoHash = HashTest.randomHash();
    InetSocketAddress[] addresses = new InetSocketAddress[6];
    for (int i = 0; i < addresses.length; i++) {
      addresses[i] = randomInetSocketAddress();
    }

    for (int i = 0; i < 4; i++) {
      announceStore.announce(infoHash, addresses[i]);
      timeSource.increaseTime(1);
    }

    TrackerResponse.PeerInfo[] peers = announceStore.getPeers(infoHash, 8);
    List<InetSocketAddress> peerAddresses = peerArrayToAddressList(peers);
    for (int i = 0; i < 4; i++) {
      assertContains(addresses[0], peerAddresses);
    }

    announceStore.announce(infoHash, addresses[0]);
    timeSource.increaseTime(1);
    announceStore.announce(infoHash, addresses[4]);
    timeSource.increaseTime(1);
    announceStore.announce(infoHash, addresses[5]);

    peers = announceStore.getPeers(infoHash, 8);
    assertEquals(4, peers.length);

    peerAddresses = peerArrayToAddressList(peers);
    assertContains(addresses[0], peerAddresses);
    assertContains(addresses[3], peerAddresses);
    assertContains(addresses[4], peerAddresses);
    assertContains(addresses[5], peerAddresses);
  }

  private List<InetSocketAddress> peerArrayToAddressList(TrackerResponse.PeerInfo[] peers) {
    return CollectionUtils.map(Arrays.asList(peers),
        new CollectionUtils.Function<TrackerResponse.PeerInfo, InetSocketAddress>() {
          public InetSocketAddress evaluate(TrackerResponse.PeerInfo source) {
            return source.getSocketAddress();
          }
        });
  }
}
