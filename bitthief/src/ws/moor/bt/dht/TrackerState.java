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

import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.tracker.TrackerResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TrackerState {

  private final PeerId ourId;

  private final int listeningPort;

  private final Set<TrackerResponse.PeerInfo> peerInfos;

  private static long FILE_VERSION = 0x01;

  public PeerId getOurId() {
    return ourId;
  }

  private TrackerState(PeerId ourId, int listeningPort, Set<TrackerResponse.PeerInfo> peerInfos) {
    this.ourId = ourId;
    this.listeningPort = listeningPort;
    this.peerInfos = peerInfos;
  }


  public void saveToFile(File file) throws IOException {
    FileOutputStream fos = new FileOutputStream(file);
    DataOutputStream os = new DataOutputStream(fos);

    os.writeLong(FILE_VERSION);
    os.writeInt(listeningPort);
    writeByteArray(os, ourId.getBytes());
    os.writeInt(peerInfos.size());
    for (TrackerResponse.PeerInfo info : peerInfos) {
      writeByteArray(os, info.toCompactLongForm());
    }

    os.close();
  }

  private void writeByteArray(DataOutputStream os, byte[] array) throws IOException {
    os.writeInt(array.length);
    os.write(array);
  }

  private static byte[] readByteArray(DataInputStream is) throws IOException {
    int length = is.readInt();
    byte[] result = new byte[length];
    is.read(result);
    return result;
  }

  public int getListeningPort() {
    return listeningPort;
  }

  public Set<TrackerResponse.PeerInfo> getPeers() {
    return peerInfos;
  }

  public static TrackerState createForTracker(DHTracker tracker) {
    PeerId ourId = tracker.getPeerId();
    RoutingTable routingTable = tracker.getRoutingTable();
    TrackerResponse.PeerInfo[] closeNodes = routingTable.getNodesCloseTo(ourId, 1024);
    return new TrackerState(ourId, tracker.getPort(), new HashSet<TrackerResponse.PeerInfo>(Arrays.asList(closeNodes)));
  }

  public static TrackerState createForFile(File file) throws IOException {
    FileInputStream fis = new FileInputStream(file);
    DataInputStream is = new DataInputStream(fis);

    long version = is.readLong();
    if (version != FILE_VERSION) {
      return null;
    }

    int port = is.readInt();
    PeerId ourId = new PeerId(readByteArray(is));
    int peerCount = is.readInt();
    Set<TrackerResponse.PeerInfo> peers = new HashSet<TrackerResponse.PeerInfo>();
    for (int i = 0; i < peerCount; i++) {
      peers.add(TrackerResponse.PeerInfo.fromCompactLongForm(readByteArray(is), 0));
    }

    is.close();

    return new TrackerState(ourId, port, peers);
  }

  public static TrackerState createNew(int listeningPort) {
    Set<TrackerResponse.PeerInfo> peers = new HashSet<TrackerResponse.PeerInfo>();
    InetSocketAddress routerAddress = new InetSocketAddress("router.bittorrent.com", 6881);
    peers.add(new TrackerResponse.PeerInfo(PeerId.createRandom(), routerAddress));

    return new TrackerState(PeerId.createRandom(), listeningPort, peers);
  }
}
