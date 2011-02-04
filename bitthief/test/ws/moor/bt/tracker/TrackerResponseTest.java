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

package ws.moor.bt.tracker;

import ws.moor.bt.bencoding.BDictionary;
import ws.moor.bt.bencoding.BEntity;
import ws.moor.bt.bencoding.BInteger;
import ws.moor.bt.bencoding.BList;
import ws.moor.bt.bencoding.BString;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.util.ArrayUtil;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.ExtendedTestCase;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO(pmoor): Javadoc
 */
public class TrackerResponseTest extends ExtendedTestCase {

  public void testMinimalCompactResponse() throws UnknownHostException {
    BDictionary<BEntity> dictionary = new BDictionary<BEntity>();
    dictionary.put(new BString("interval"), new BInteger(900));
    dictionary.put(new BString("peers"), new BString(new byte[] {0x10, 0x20, 0x30, 0x40, 0x50, 0x60}));

    TrackerResponse response = new TrackerResponse(dictionary);
    assertEquals(900, response.getAnnounceInterval());
    assertEquals(1, response.getPeerInformation().length);

    byte[] address = new byte[] {0x10, 0x20, 0x30, 0x40};
    TrackerResponse.PeerInfo reference =
        new TrackerResponse.PeerInfo(null, InetAddress.getByAddress(address), 0x5060);
    assertEquals(reference, response.getPeerInformation()[0]);
  }

  public void testMinimalNormalResponse() throws UnknownHostException {
    BDictionary<BEntity> dictionary = new BDictionary<BEntity>();
    dictionary.put(new BString("interval"), new BInteger(1800));
    PeerId peerId = PeerId.createRandomMainlineId();
    BDictionary<BEntity> peerInfo = createPeerRecord(peerId, "192.132.2.16", 777);
    BList<BDictionary> peerInfoList = new BList<BDictionary>();
    peerInfoList.add(peerInfo);
    dictionary.put(new BString("peers"), peerInfoList);

    TrackerResponse response = new TrackerResponse(dictionary);
    assertEquals(1800, response.getAnnounceInterval());
    assertEquals(1, response.getPeerInformation().length);

    byte[] address = new byte[] {(byte) 192, (byte) 132, 2, 16};
    TrackerResponse.PeerInfo reference =
        new TrackerResponse.PeerInfo(peerId, InetAddress.getByAddress(address), 777);
    assertEquals(reference, response.getPeerInformation()[0]);
  }

  public void testUsualResponse() throws Exception {
    BDictionary<BEntity> dictionary = new BDictionary<BEntity>();
    dictionary.put(new BString("interval"), new BInteger(2700));
    dictionary.put(new BString("incomplete"), new BInteger(67));
    dictionary.put(new BString("complete"), new BInteger(133));
    dictionary.put(new BString("min interval"), new BInteger(1800));
    dictionary.put(new BString("warning"), new BString("This Is A Friendly Warning"));
    PeerId peerIdA = PeerId.createRandomMainlineId();
    PeerId peerIdB = PeerId.createRandomMainlineId();
    PeerId peerIdC = PeerId.createRandomMainlineId();

    BList<BDictionary> peerInfoList = new BList<BDictionary>();
    peerInfoList.add(createPeerRecord(peerIdA, "192.132.2.16", 4242));
    peerInfoList.add(createPeerRecord(peerIdB, "112.105.3.27", 8080));
    peerInfoList.add(createPeerRecord(peerIdC, "232.88.7.55", 142));
    dictionary.put(new BString("peers"), peerInfoList);

    TrackerResponse response = new TrackerResponse(dictionary);
    assertEquals(2700, response.getAnnounceInterval());
    assertEquals(3, response.getPeerInformation().length);
    assertEquals(133, response.getCompletePeerCount());
    assertEquals(67, response.getIncompletePeerCount());
    assertEquals(1800, response.getMinimalAnnounceInterval());
    assertTrue(response.hasWarning());
    assertEquals("This Is A Friendly Warning", response.getWarning());

    Collection<TrackerResponse.PeerInfo> peerInfoCollection =
        new ArrayList<TrackerResponse.PeerInfo>();

    byte[] address = new byte[] {(byte) 192, (byte) 132, 2, 16};
    TrackerResponse.PeerInfo reference =
        new TrackerResponse.PeerInfo(peerIdA, InetAddress.getByAddress(address), 4242);
    assertEquals(reference, response.getPeerInformation()[0]);
    peerInfoCollection.add(reference);

    address = new byte[] {(byte) 112, (byte) 105, 3, 27};
    reference = new TrackerResponse.PeerInfo(peerIdB, InetAddress.getByAddress(address), 8080);
    assertEquals(reference, response.getPeerInformation()[1]);
    peerInfoCollection.add(reference);

    address = new byte[] {(byte) 232, (byte) 88, 7, 55};
    reference = new TrackerResponse.PeerInfo(peerIdC, InetAddress.getByAddress(address), 142);
    assertEquals(reference, response.getPeerInformation()[2]);
    peerInfoCollection.add(reference);

    assertEquals(peerInfoCollection, response.getPeerInformationCollection());
  }

  public void testErrorResponse() {
    BDictionary<BEntity> dictionary = new BDictionary<BEntity>();
    dictionary.put(new BString("failure reason"), new BString("This Is An Error"));

    TrackerResponse response = new TrackerResponse(dictionary);
    assertTrue(response.hasError());
    assertEquals("This Is An Error", response.getError());
  }

  public void testPeerInfo() throws UnknownHostException {
    PeerId peerId = PeerId.createRandomMainlineId();
    TrackerResponse.PeerInfo info =
        TrackerResponse.PeerInfo.fromDictionary(
            createPeerRecord(peerId, "192.168.0.5", 7777));
    assertEquals(peerId, info.getPeerId());
    assertEquals(InetAddress.getByAddress(ByteUtil.newByteArray(0xc0, 0xa8, 0x00, 0x05)), info.getAddress());
    assertEquals(7777, info.getPort());
  }

  public void testGetSocketAddress() throws UnknownHostException {
    TrackerResponse.PeerInfo info =
        TrackerResponse.PeerInfo.fromDictionary(
            createPeerRecord(PeerId.createRandomMainlineId(), "80.74.132.92", 4242));
    InetSocketAddress address = info.getSocketAddress();
    assertEquals(4242, address.getPort());
    assertEquals(InetAddress.getByAddress(ByteUtil.newByteArray(80, 74, 132, 92)), address.getAddress());
  }

  public void testConstructPeerInfoWithNullAddress() {
    try {
      new TrackerResponse.PeerInfo(PeerId.createRandomMainlineId(), null, 4242);
      fail("should not succeed");
    } catch (NullPointerException e) {
      // expected
    }
  }

  public void testToCompactForm() throws UnknownHostException {
    InetAddress address = InetAddress.getByAddress(ByteUtil.newByteArray(14, 18, 22, 10));
    int port = 0xff72;
    TrackerResponse.PeerInfo info =
        new TrackerResponse.PeerInfo(null, address, port);
    assertArrayEquals(ByteUtil.newByteArray(14, 18, 22, 10, 0xff, 0x72), info.toCompactForm());
  }

  public void testToCompactLongForm() throws UnknownHostException {
    InetAddress address = InetAddress.getByAddress(ByteUtil.newByteArray(14, 18, 22, 10));
    int port = 0xff72;
    PeerId peerId = PeerId.createRandomMainlineId();
    TrackerResponse.PeerInfo info =
        new TrackerResponse.PeerInfo(peerId, address, port);

    byte[] result = info.toCompactLongForm();
    assertEquals(26, result.length);
    assertArrayEquals(peerId.getBytes(), ArrayUtil.subArray(result, 0, 20));
    assertArrayEquals(ByteUtil.newByteArray(14, 18, 22, 10, 0xff, 0x72), ArrayUtil.subArray(result, 20, 6));
  }

  public void testFromCompactLongForm() {
    TrackerResponse.PeerInfo info =
        TrackerResponse.PeerInfo.fromDictionary(
            createPeerRecord(PeerId.createRandomMainlineId(), "80.74.132.92", 4242));
    byte[] bytes = info.toCompactLongForm();
    TrackerResponse.PeerInfo info2 = TrackerResponse.PeerInfo.fromCompactLongForm(bytes, 0);
    assertEquals(info, info2);
  }

  public void testInvalidPort() {
    try {
      new TrackerResponse.PeerInfo(PeerId.createRandom(), randomInetAddress(), 0);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      new TrackerResponse.PeerInfo(PeerId.createRandom(), randomInetAddress(), 65536);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  private BDictionary<BEntity> createPeerRecord(PeerId peerId, String ipAddress, int port) {
    BDictionary<BEntity> peerInfo = new BDictionary<BEntity>();
    peerInfo.put(new BString("peer id"), new BString(peerId.getBytes()));
    peerInfo.put(new BString("ip"), new BString(ipAddress));
    peerInfo.put(new BString("port"), new BInteger(port));
    return peerInfo;
  }
}
