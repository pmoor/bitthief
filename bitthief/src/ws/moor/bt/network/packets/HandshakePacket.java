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

package ws.moor.bt.network.packets;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import ws.moor.bt.torrent.Hash;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.util.LoggingUtil;

import java.util.Arrays;

/**
 * TODO(pmoor): Javadoc
 */
public class HandshakePacket implements Packet {

  public static final String BITTORRENT_PROTOCOL = "BitTorrent protocol";

  private static final int RESERVED_LENGTH = 8;

  private final String protocol;
  private final byte[] reserved;
  private final Hash infoHash;
  private final PeerId peerId;

  private static final Logger logger = LoggingUtil.getLogger(HandshakePacket.class);

  public HandshakePacket(Hash infoHash, PeerId peerId) {
    this(BITTORRENT_PROTOCOL, new byte[RESERVED_LENGTH], infoHash, peerId);
  }

  private HandshakePacket(String protocol, byte[] reserved, Hash infoHash, PeerId peerId) {
    this.reserved = reserved;
    this.protocol = protocol;
    this.infoHash = infoHash;
    this.peerId = peerId;
  }

  public int writeIntoBuffer(byte[] buffer, int offset) {
    if (buffer.length < offset + getPayloadLength()) {
      throw new IllegalArgumentException(
          "buffer is too small, should be at least " + getPayloadLength());
    }
    byte[] protocolBytes = protocol.getBytes();
    buffer[offset] = (byte) protocolBytes.length;
    System.arraycopy(protocolBytes, 0, buffer,
        offset + 1, protocolBytes.length);

    System.arraycopy(reserved, 0, buffer,
        offset + 1 + protocolBytes.length, RESERVED_LENGTH);

    byte[] hash = infoHash.getBytes();
    System.arraycopy(hash, 0, buffer,
        offset + 1 + protocolBytes.length + RESERVED_LENGTH, hash.length);

    if (hasPeerId()) {
      byte[] id = peerId.getBytes();
      System.arraycopy(id, 0, buffer,
          offset + 1 + protocolBytes.length + RESERVED_LENGTH + hash.length, id.length);
    }

    return offset + getPayloadLength();
  }

  public int getPayloadLength() {
    return 1 + protocol.getBytes().length
        + RESERVED_LENGTH + Hash.LENGTH + (hasPeerId() ? PeerId.LENGTH : 0);
  }

  public int getId() {
    throw new UnsupportedOperationException("this packet type does not have an ID");
  }

  public void handle(PacketHandler handler) {
    handler.handleHandshakePacket(this);
  }

  public String getProtocol() {
    return protocol;
  }

  public Hash getInfoHash() {
    return infoHash;
  }

  public PeerId getPeerId() {
    return peerId;
  }

  public boolean hasPeerId() {
    return peerId != null;
  }

  public boolean equals(Object other) {
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    final HandshakePacket that = (HandshakePacket) other;
    if (!infoHash.equals(that.infoHash)) {
      return false;
    }
    if (hasPeerId() ? !peerId.equals(that.peerId) : that.peerId != null) {
      return false;
    }
    if (!protocol.equals(that.protocol)) {
      return false;
    }
    return Arrays.equals(reserved, that.reserved);
  }

  public int hashCode() {
    int result;
    result = protocol.hashCode();
    result = 29 * result + Arrays.hashCode(reserved);
    result = 29 * result + infoHash.hashCode();
    result = 29 * result + (peerId != null ? peerId.hashCode() : 0);
    return result;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("InfoHash: ").append(infoHash.toString()).append("\n");
    builder.append("Reserved: ").append(Hex.encodeHex(reserved));
    if (hasPeerId()) {
      builder.append("\n");
      builder.append("PeerId: ").append(peerId);
    }
    return builder.toString();
  }

  public static PacketConstructor<HandshakePacket> getConstructor() {
    return new Constructor();
  }

  private static class Constructor implements PacketConstructor<HandshakePacket> {

    public int getId() {
      throw new UnsupportedOperationException("this packet type does not have an ID");
    }

    public HandshakePacket constructPacket(byte[] buffer, int offset, int length) {
      if (offset + length > buffer.length) {
        return null;
      }

      int protocolLength = buffer[offset];
      int bytesNeeded = 1 + protocolLength + RESERVED_LENGTH + Hash.LENGTH;
      int bytesNeededFull = bytesNeeded + PeerId.LENGTH;
      if (bytesNeeded != length && bytesNeededFull != length) {
        logger.debug("not the right amount of bytes available in handshake constructor");
        return null;
      }
      byte[] protocol = new byte[protocolLength];
      System.arraycopy(buffer, offset + 1,
          protocol, 0, protocolLength);
      byte[] reserved = new byte[RESERVED_LENGTH];
      System.arraycopy(buffer, offset + 1 + protocolLength,
          reserved, 0, RESERVED_LENGTH);
      byte[] hash = new byte[Hash.LENGTH];
      System.arraycopy(buffer, offset + 1 + protocolLength + RESERVED_LENGTH,
          hash, 0, Hash.LENGTH);
      Hash infoHash = new Hash(hash);
      PeerId peerId = null;
      if (length == bytesNeededFull) {
        byte[] id = new byte[PeerId.LENGTH];
        System.arraycopy(buffer, offset + 1 + protocolLength + RESERVED_LENGTH + Hash.LENGTH,
            id, 0, PeerId.LENGTH);
        peerId = new PeerId(id);
      }
      return new HandshakePacket(new String(protocol), reserved, infoHash, peerId);
    }
  }
}
