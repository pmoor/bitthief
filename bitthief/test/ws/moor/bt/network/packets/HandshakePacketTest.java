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

import org.easymock.classextension.EasyMock;
import ws.moor.bt.torrent.Hash;
import ws.moor.bt.torrent.HashTest;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class HandshakePacketTest extends ExtendedTestCase {

  Hash infoHash;
  PeerId peerId;
  HandshakePacket packet;

  public void testSimpleCreation() {
    assertEquals("BitTorrent protocol", packet.getProtocol());
    assertEquals(infoHash, packet.getInfoHash());
    assertEquals(peerId, packet.getPeerId());
  }

  public void testShortBuffer() {
    try {
      packet.writeIntoBuffer(new byte[0], 0);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testNullBuffer() {
    try {
      packet.writeIntoBuffer(null, 0);
      fail("should fail");
    } catch (NullPointerException e) {
      // expected
    }
  }

  public void testWriteBuffer() {
    byte[] buffer = new byte[128];
    int newOffset = packet.writeIntoBuffer(buffer, 7);
    assertEquals(7 + 68, newOffset);

    // protocol
    assertEquals(19, buffer[7]);
    assertEquals("BitTorrent protocol", new String(buffer, 8, 19));

    // reserved
    byte[] subarray = new byte[8];
    System.arraycopy(buffer, 27, subarray, 0, 8);
    assertArrayEquals(new byte[8], subarray);

    // info hash
    subarray = new byte[20];
    System.arraycopy(buffer, 35, subarray, 0, 20);
    assertEquals(infoHash, new Hash(subarray));

    // peer id
    subarray = new byte[20];
    System.arraycopy(buffer, 55, subarray, 0, 20);
    assertEquals(peerId, new PeerId(subarray));
  }

  public void testConstructAndEquals() {
    byte[] buffer = new byte[128];
    int newOffset = packet.writeIntoBuffer(buffer, 3);
    assertEquals(3 + 68, newOffset);

    HandshakePacket packet2 = HandshakePacket.getConstructor().constructPacket(buffer, 3, 68);
    assertEquals(packet, packet2);
    assertEquals(packet.hashCode(), packet2.hashCode());
  }

  public void testTrackerNatCheckMessage() {
    byte[] buffer = new byte[48];
    HandshakePacket packet = new HandshakePacket(HashTest.randomHash(), null);
    int newOffset = packet.writeIntoBuffer(buffer, 0);
    assertEquals(48, newOffset);

    HandshakePacket packet2 = HandshakePacket.getConstructor().constructPacket(buffer, 0, 48);
    assertEquals(packet, packet2);
  }

  public void testConstructorWithWrongSize() {
    byte[] buffer = new byte[128];
    buffer[0] = 19;

    HandshakePacket packet =
        HandshakePacket.getConstructor().constructPacket(buffer, 0, 47);
    assertNull(packet);

    packet = HandshakePacket.getConstructor().constructPacket(buffer, 0, 48);
    assertNotNull(packet);

    packet = HandshakePacket.getConstructor().constructPacket(buffer, 0, 49);
    assertNull(packet);

    packet = HandshakePacket.getConstructor().constructPacket(buffer, 0, 67);
    assertNull(packet);

    packet = HandshakePacket.getConstructor().constructPacket(buffer, 0, 68);
    assertNotNull(packet);

    packet = HandshakePacket.getConstructor().constructPacket(buffer, 0, 69);
    assertNull(packet);
  }

  public void setUp() throws Exception {
    super.setUp();
    infoHash = HashTest.randomHash();
    peerId = PeerId.createRandomMainlineId();
    packet = new HandshakePacket(infoHash, peerId);
  }

  public void testHandler() {
    PacketHandler handler = EasyMock.createMock(PacketHandler.class);
    handler.handleHandshakePacket(EasyMock.same(packet));
    EasyMock.replay(handler);
    packet.handle(handler);
    EasyMock.verify(handler);
  }
}
