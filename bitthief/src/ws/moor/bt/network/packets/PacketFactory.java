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
import ws.moor.bt.util.ArrayUtil;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.LoggingUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO(pmoor): Javadoc
 */
public class PacketFactory {

  private PacketConstructor<HandshakePacket> handshakeConstructor;
  private PacketConstructor<KeepAlivePacket> keepaliveConstructor;

  private final Map<Integer, PacketConstructor> constructors;

  private static final Logger logger = LoggingUtil.getLogger(PacketFactory.class);

  public PacketFactory() {
    constructors = new HashMap<Integer, PacketConstructor>();
    loadDefaultConstructors();
  }

  private void loadDefaultConstructors() {
    setHandshakeConstructor(HandshakePacket.getConstructor());
    setKeepAliveConstructor(KeepAlivePacket.getConstructor());
    addConstructor(ChokePacket.getConstructor());
    addConstructor(UnchokePacket.getConstructor());
    addConstructor(InterestedPacket.getConstructor());
    addConstructor(NotInterestedPacket.getConstructor());
    addConstructor(HavePacket.getConstructor());
    addConstructor(BitFieldPacket.getConstructor());
    addConstructor(RequestPacket.getConstructor());
    addConstructor(PiecePacket.getConstructor());
    addConstructor(CancelPacket.getConstructor());
  }

  public void setHandshakeConstructor(PacketConstructor<HandshakePacket> constructor) {
    handshakeConstructor = constructor;
  }

  public void setKeepAliveConstructor(PacketConstructor<KeepAlivePacket> constructor) {
    keepaliveConstructor = constructor;
  }

  public void addConstructor(PacketConstructor constructor) {
    int id = constructor.getId();
    logger.info("registering packet constructor for id " + id);
    constructors.put(id, constructor);
  }

  public HandshakePacket buildHandshakePacket(byte[] buffer, int offset, int length) {
    return handshakeConstructor.constructPacket(buffer, offset, length);
  }

  public Packet buildPacket(byte[] buffer, int offset, int length) {
    if (length < 4) {
      throw new IllegalArgumentException("packets are 4 bytes minimal");
    }
    if (offset + length > buffer.length) {
      throw new IllegalArgumentException("length is longer than buffer");
    }
    int packetLength = ByteUtil.b32_to_int(buffer, offset);
    if (packetLength > length) {
      throw new IllegalArgumentException("packet is larger");
    }

    offset += 4;
    if (packetLength == 0) {
      return keepaliveConstructor.constructPacket(buffer, offset, packetLength);
    } else if (packetLength > 0) {
      int packetCode = buffer[offset];
      offset++;
      PacketConstructor constructor = constructors.get(packetCode);
      if (constructor == null) {
        logger.warn("no constructor for packet code " + packetCode + " found");
        logger.warn("buffer content is: " + Hex.encodeHex(ArrayUtil.subArray(buffer, offset, length)));
        return null;
      }
      return constructor.constructPacket(buffer, offset, packetLength - 1);
    }
    return null;
  }

  public void serializePacket(Packet packet, byte[] buffer, int offset) {
    int spaceNeeded = getExpectedBytesOnWire(packet);
    if (buffer.length < offset + spaceNeeded) {
      throw new IllegalArgumentException("buffer is too small");
    }
    if (packet.getClass() == HandshakePacket.class) {
      serializeHandshakePacket((HandshakePacket) packet, buffer, offset);
    } else if (packet.getClass() == KeepAlivePacket.class) {
      serializeKeepAlivePacket((KeepAlivePacket) packet, buffer, offset);
    } else {
      serializeGenericPacket(packet, buffer, offset);
    }
  }

  private void serializeHandshakePacket(HandshakePacket handshakePacket, byte[] buffer, int offset) {
    handshakePacket.writeIntoBuffer(buffer, offset);
  }

  private void serializeKeepAlivePacket(KeepAlivePacket keepAlivePacket, byte[] buffer, int offset) {
    ByteUtil.int_to_b32(0, buffer, offset);
  }

  private void serializeGenericPacket(Packet packet, byte[] buffer, int offset) {
    int payload = packet.getPayloadLength();
    ByteUtil.int_to_b32(payload + 1, buffer, offset);
    offset += 4;
    buffer[offset] = (byte) packet.getId();
    offset++;
    packet.writeIntoBuffer(buffer, offset);
  }

  public int getExpectedBytesOnWire(Packet packet) {
    if (packet.getClass() == HandshakePacket.class) {
      return packet.getPayloadLength();
    } else if (packet.getClass() == KeepAlivePacket.class) {
      return 4;
    } else {
      return 5 + packet.getPayloadLength();
    }
  }
}
