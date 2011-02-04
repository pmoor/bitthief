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

import ws.moor.bt.torrent.Hash;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.util.ByteUtil;

public class TrackerRequest {

  private static final int DEFAULT_PEERS_WANTED = 50;

  public static enum Event {
    STARTED, COMPLETED, STOPPED
  }

  private final long bytesLeft;

  private final long bytesDownloaded;

  private final long bytesUploaded;

  private final Event event;

  private final int listeningPort;

  private final PeerId peerId;

  private final Hash infoHash;

  private int peersWanted;

  private String ipAddress;

  private boolean compactFormatWanted;

  private byte[] key;

  public TrackerRequest(Hash infoHash,
                        PeerId peerId,
                        int listeningPort,
                        Event event,
                        long bytesUploaded,
                        long bytesDownloaded,
                        long bytesLeft) {
    if (infoHash == null) {
      throw new NullPointerException("infoHash is null");
    }
    this.infoHash = infoHash;

    if (peerId == null) {
      throw new NullPointerException("peerId is null");
    }
    this.peerId = peerId;

    if (listeningPort < 1 || listeningPort > 65535) {
      throw new IllegalArgumentException("invalid listening port");
    }
    this.listeningPort = listeningPort;

    this.event = event;

    if (bytesUploaded < 0) {
      throw new IllegalArgumentException("invalid uploaded count");
    }
    this.bytesUploaded = bytesUploaded;

    if (bytesDownloaded < 0) {
      throw new IllegalArgumentException("invalid downloaded count");
    }
    this.bytesDownloaded = bytesDownloaded;

    if (bytesLeft < 0) {
      throw new IllegalArgumentException("invalid left count");
    }
    this.bytesLeft = bytesLeft;

    this.peersWanted = 0;
    this.ipAddress = null;
    this.compactFormatWanted = false;
    this.key = null;
  }

  public Hash getInfoHash() {
    return infoHash;
  }

  public PeerId getPeerId() {
    return peerId;
  }

  public String getIPAddress() {
    return ipAddress;
  }

  public int getListeningPort() {
    return listeningPort;
  }

  public long getUploaded() {
    return bytesUploaded;
  }

  public long getDownloaded() {
    return bytesDownloaded;
  }

  public long getLeft() {
    return bytesLeft;
  }

  public Event getEvent() {
    return event;
  }

  public int getPeersWanted() {
    return (peersWanted > 0) ? peersWanted : DEFAULT_PEERS_WANTED;
  }

  public boolean getCompactFormatWanted() {
    return compactFormatWanted;
  }

  public void setCompactFormatWanted(boolean compactFormatWanted) {
    this.compactFormatWanted = compactFormatWanted;
  }

  public void setPeersWanted(int peersWanted) {
    this.peersWanted = peersWanted;
  }

  public byte[] getKey() {
    return key;
  }

  public void setKey(byte[] key) {
    this.key = key;
  }

  public String getHttpEncodedParameters() {
    StringBuilder result = new StringBuilder();
    appendOriginalParameters(result);
    return result.toString();
  }

  public String getHttpEncodedExtendedParameters() {
    StringBuilder result = new StringBuilder();
    appendOriginalParameters(result);
    if (getCompactFormatWanted()) {
      appendParameter(result, "compact", "1");
    }
    if (getKey() != null) {
      appendParameter(result, "key", ByteUtil.urlEncode(getKey()));
    }
    return result.toString();
  }

  private void appendOriginalParameters(StringBuilder result) {
    appendParameter(result, "info_hash", ByteUtil.urlEncode(getInfoHash().getBytes()));
    appendParameter(result, "peer_id", ByteUtil.urlEncode(getPeerId().getBytes()));
    if (getIPAddress() != null) {
      appendParameter(result, "ip", getIPAddress());
    }
    appendParameter(result, "port", Integer.toString(getListeningPort()));
    appendParameter(result, "uploaded", Long.toString(getUploaded()));
    appendParameter(result, "downloaded", Long.toString(getDownloaded()));
    appendParameter(result, "left", Long.toString(getLeft()));
    if (event != null) {
      appendParameter(result, "event", getEvent().toString().toLowerCase());
    }
    if (peersWanted > 0) {
      appendParameter(result, "numwant", Integer.toString(getPeersWanted()));
    }
  }

  private void appendParameter(StringBuilder builder, String name, String value) {
    if (builder.length() > 0) {
      builder.append("&");
    }
    builder.append(name);
    builder.append("=");
    builder.append(value);
  }
}
