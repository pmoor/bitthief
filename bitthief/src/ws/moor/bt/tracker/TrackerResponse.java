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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

/**
 * TODO(pmoor): Javadoc
 */
public class TrackerResponse {

  private final long completePeers;
  private final long incompletePeers;
  private final long announceInterval;
  private final long minimalAnnounceInterval;
  private final String warning;
  private final String error;

  private final PeerInfo[] peerInfo;

  public TrackerResponse(BDictionary dictionary) {
    completePeers = getLong(dictionary, "complete", -1);
    incompletePeers = getLong(dictionary, "incomplete", -1);
    announceInterval = getLong(dictionary, "interval", -1);
    minimalAnnounceInterval = getLong(dictionary, "min interval", -1);
    warning = getString(dictionary, "warning", null);
    error = getString(dictionary, "failure reason", null);

    PeerInfo[] tmpPeerInfo = new PeerInfo[0];
    BEntity peerEntity = dictionary.getByString("peers");
    if (peerEntity instanceof BString) {
      // packed binary format
      tmpPeerInfo = parsePeerInformation((BString) peerEntity);
    } else if (peerEntity instanceof BList) {
      // list/dictionary format
      tmpPeerInfo = parsePeerInformation((BList<BDictionary>) peerEntity);
    } else if (!hasError()) {
      throw new IllegalArgumentException("impossible to parse peer list");
    }
    peerInfo = tmpPeerInfo;
  }

  private PeerInfo[] parsePeerInformation(BList<BDictionary> data) {
    PeerInfo[] result = new PeerInfo[data.size()];
    int i = 0;
    for (BDictionary dictionary : data) {
      result[i++] = PeerInfo.fromDictionary(dictionary);
    }
    return result;
  }

  private PeerInfo[] parsePeerInformation(BString data) {
    byte[] value = data.getBytes();
    if (value.length % 6 != 0) {
      throw new IllegalArgumentException("peer information length must be a multiple of 6");
    }
    int peers = value.length / 6;
    PeerInfo[] result = new PeerInfo[peers];
    for (int i = 0; i < peers; i++) {
      result[i] = PeerInfo.fromCompactForm(value, 6 * i);
    }
    return result;
  }

  public long getCompletePeerCount() {
    return completePeers;
  }

  public long getIncompletePeerCount() {
    return incompletePeers;
  }

  public long getAnnounceInterval() {
    return announceInterval;
  }

  public long getMinimalAnnounceInterval() {
    return minimalAnnounceInterval;
  }

  private String getString(BDictionary dictionary, String key, String defaultValue) {
    BEntity entity = dictionary.getByString(key);
    if (entity == null) {
      return defaultValue;
    } else if (!(entity instanceof BString)) {
      throw new IllegalArgumentException("Not a String: " + entity);
    }
    return ((BString) entity).toString();
  }

  private long getLong(BDictionary dictionary, String key, long defaultValue) {
    BEntity entity = dictionary.getByString(key);
    if (entity == null) {
      return defaultValue;
    } else if (!(entity instanceof BInteger)) {
      throw new IllegalArgumentException("Not an Integer: " + entity);
    }
    return ((BInteger) entity).longValue();
  }

  public PeerInfo[] getPeerInformation() {
    return peerInfo;
  }

  public boolean hasWarning() {
    return warning != null;
  }

  public String getWarning() {
    return warning;
  }

  public String getError() {
    return error;
  }

  public boolean hasError() {
    return error != null;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (hasError()) {
      builder.append("Error: ").append(getError()).append("\n");
    }
    if (hasWarning()) {
      builder.append("Warning: ").append(getWarning()).append("\n");
    }
    builder.append("announce interval: ").append(getAnnounceInterval());
    builder.append(" (").append(getMinimalAnnounceInterval()).append(")\n");
    builder.append("Leechers: ").append(getIncompletePeerCount()).append("\n");
    builder.append("Seeders: ").append(getCompletePeerCount()).append("\n");
    builder.append(peerInfo.length).append(" Peers:\n");
    for (PeerInfo peer : peerInfo) {
      builder.append("\t").append(peer).append("\n");
    }
    return builder.toString();
  }

  public Collection<PeerInfo> getPeerInformationCollection() {
    return Arrays.asList(getPeerInformation());
  }

  public static class PeerInfo {
    final PeerId peerId;
    final InetAddress address;
    final int port;

    public PeerInfo(PeerId peerId, InetAddress address, int port) {
      if (address == null) {
        throw new NullPointerException("address cannot be null");
      } else if (port <= 0 || port > 0xffff) {
        throw new IllegalArgumentException("port is not valid: " + port);
      }
      this.peerId = peerId;
      this.address = address;
      this.port = port;
    }

    public PeerInfo(PeerId peerId, InetSocketAddress address) {
      this(peerId, address.getAddress(), address.getPort());
    }

    public static PeerInfo fromCompactForm(byte[] bytes, int offset) {
      return fromCompactFormWithId(bytes, offset, null);
    }

    private static PeerInfo fromCompactFormWithId(byte[] bytes, int offset, PeerId id) {
      if (bytes.length < offset + 6) {
        throw new IndexOutOfBoundsException();
      }
      byte[] addressBytes = new byte[4];
      System.arraycopy(bytes, offset, addressBytes, 0, 4);
      InetAddress address;
      try {
        address = InetAddress.getByAddress(addressBytes);
      } catch (UnknownHostException e) {
        return null;
      }
      int port = (bytes[offset + 4] & 0xff) << 8 | (bytes[offset + 5] & 0xff);
      return new PeerInfo(id, address, port);
    }

    public static PeerInfo fromCompactLongForm(byte[] bytes, int offset) {
      if (bytes.length < offset + PeerId.LENGTH + 6) {
        throw new IndexOutOfBoundsException();
      }
      byte[] idBytes = new byte[PeerId.LENGTH];
      System.arraycopy(bytes, offset, idBytes, 0, PeerId.LENGTH);
      PeerId id = new PeerId(idBytes);
      return fromCompactFormWithId(bytes, offset + PeerId.LENGTH, id);
    }

    public byte[] toCompactForm() {
      byte[] addr = address.getAddress();
      if (addr.length != 4) {
        throw new IllegalStateException("something is wrong, should be an IPv4 address");
      }
      byte[] result = new byte[6];
      System.arraycopy(addr, 0, result, 0, 4);
      result[4] = (byte) ((port >> 8) & 0xff);
      result[5] = (byte) (port & 0xff);
      return result;
    }

    public byte[] toCompactLongForm() {
      if (peerId == null) {
        throw new NullPointerException("peer id must be set for this format");
      }
      byte[] shortPart = toCompactForm();
      byte[] result = new byte[PeerId.LENGTH + shortPart.length];
      System.arraycopy(shortPart, 0, result, PeerId.LENGTH, shortPart.length);
      System.arraycopy(peerId.getBytes(), 0, result, 0, PeerId.LENGTH);
      return result;
    }

    public static PeerInfo fromDictionary(BDictionary dictionary) {
      PeerId id = new PeerId(((BString) dictionary.getByString("peer id")).getBytes());
      InetAddress address = null;
      try {
        address = InetAddress.getByName(dictionary.getByString("ip").toString());
      } catch (UnknownHostException e) {
        return null;
      }
      int port = ((BInteger) dictionary.getByString("port")).intValue();
      return new PeerInfo(id, address, port);
    }

    public boolean equals(Object other) {
      if (other == null || getClass() != other.getClass()) {
        return false;
      }
      final PeerInfo peerInfo = (PeerInfo) other;
      if (port != peerInfo.port) {
        return false;
      }
      if (!address.equals(peerInfo.address)) {
        return false;
      }
      if (peerId == null) {
        return peerInfo.peerId == null;
      }
      return peerId.equals(peerInfo.peerId);
    }

    public int hashCode() {
      int result = (peerId != null) ? peerId.hashCode() : 7;
      result = 29 * result + address.hashCode();
      result = 29 * result + port;
      return result;
    }

    public String toString() {
      StringBuilder builder = new StringBuilder();
      if (peerId != null) {
        builder.append(peerId).append("@");
      }
      builder.append(address.getHostAddress()).append(":").append(port);
      return builder.toString();
    }

    public PeerId getPeerId() {
      return peerId;
    }

    public InetAddress getAddress() {
      return address;
    }

    public int getPort() {
      return port;
    }

    public InetSocketAddress getSocketAddress() {
      return new InetSocketAddress(getAddress(), getPort());
    }
  }
}
