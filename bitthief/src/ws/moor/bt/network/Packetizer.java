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

package ws.moor.bt.network;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import ws.moor.bt.network.packets.Packet;
import ws.moor.bt.network.packets.PacketFactory;
import ws.moor.bt.util.ArrayUtil;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.LoggingUtil;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class Packetizer {

  private byte[] pendingData = new byte[0];

  private List<Packet> pendingPackets = new LinkedList<Packet>();

  private final PacketFactory factory;
  private boolean gotHandshake = false;
  private boolean erroneous = false;

  private static final Logger logger = LoggingUtil.getLogger(Packetizer.class);

  public Packetizer(PacketFactory factory) {
    this.factory = factory;
  }

  public synchronized boolean packetAvailable() {
    assertNotErroneous();
    return pendingPackets.size() > 0;
  }

  public synchronized void addData(byte[] bytes) {
    assertNotErroneous();
    pendingData = ArrayUtil.append(pendingData, bytes);
    stripPackets();
  }

  public synchronized void addData(ByteBuffer buffer, int length) {
    assertNotErroneous();
    int oldOffset = pendingData.length;
    pendingData = ArrayUtil.resize(pendingData, pendingData.length + length);
    buffer.get(pendingData, oldOffset, length);
    stripPackets();
  }

  private void stripPackets() {
    while (true) {
      int nextPacketLength = nextPacketLength();
      if (nextPacketLength == -1 || nextPacketLength > pendingData.length) {
        return;
      }
      byte[] packetData = ArrayUtil.subArray(pendingData, 0, nextPacketLength);
      pendingData = ArrayUtil.cutFront(pendingData, nextPacketLength);
      buildPacket(packetData, nextPacketLength);
    }
  }

  private void buildPacket(byte[] packetData, int nextPacketLength) {
    Packet packet;
    if (!gotHandshake) {
      gotHandshake = true;
      packet = factory.buildHandshakePacket(packetData, 0, nextPacketLength);
    } else {
      packet = factory.buildPacket(packetData, 0, nextPacketLength);
    }
    if (packet == null) {
      logger.warn("could not build a packet out of the first " +
          nextPacketLength + " bytes of " + new String(Hex.encodeHex(packetData)));
    } else {
      pendingPackets.add(packet);
    }
  }

  private int nextPacketLength() {
    if (pendingData.length < 4) {
      return -1;
    }
    if (!gotHandshake) {
      int protocolSizeInBuffer = pendingData[0] & 0xff;
      if (protocolSizeInBuffer < 0 || protocolSizeInBuffer > 64) {
        logger.warn("weird protocol length: " + protocolSizeInBuffer);
        putInErrorState();
        return -1;
      }
      if (pendingData.length == 48) {
        return 48;
      }
      return 1 + protocolSizeInBuffer + 8 + 40;
    } else {
      int sizeInBuffer = ByteUtil.b32_to_int(pendingData, 0);
      if (sizeInBuffer < 0 || sizeInBuffer > 1 * 1024 * 1024) {
        logger.warn("weird packet size: " + sizeInBuffer);
        putInErrorState();
        return -1;
      }
      return 4 + sizeInBuffer;
    }
  }

  private void putInErrorState() {
    erroneous = true;
  }

  private void assertNotErroneous() throws IllegalStateException {
    if (erroneous) {
      throw new IllegalStateException("this paketiser is erroneous, cannot be used anymore");
    }
  }

  public int pendingData() {
    assertNotErroneous();
    return pendingData.length;
  }

  public synchronized Packet getNextPacket() {
    assertNotErroneous();
    if (!packetAvailable()) {
      return null;
    }
    return pendingPackets.remove(0);
  }
}
