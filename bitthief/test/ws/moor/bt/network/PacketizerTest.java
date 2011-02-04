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

import org.easymock.classextension.EasyMock;
import ws.moor.bt.network.packets.HandshakePacket;
import ws.moor.bt.network.packets.KeepAlivePacket;
import ws.moor.bt.network.packets.PacketFactory;
import ws.moor.bt.torrent.HashTest;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.util.ExtendedTestCase;

import java.nio.ByteBuffer;

/**
 * TODO(pmoor): Javadoc
 */
public class PacketizerTest extends ExtendedTestCase {

  private Packetizer paketizer;
  private PacketFactory packetFactory;

  protected void setUp() throws Exception {
    super.setUp();
    packetFactory = EasyMock.createMock(PacketFactory.class);
    paketizer = new Packetizer(packetFactory);
  }

  protected void tearDown() throws Exception {
    EasyMock.verify(packetFactory);
    super.tearDown();
  }

  public void testEmpty() {
    EasyMock.replay(packetFactory);
    assertFalse(paketizer.packetAvailable());
  }

  public void testStillEmptyAfterSomeData() {
    EasyMock.replay(packetFactory);
    paketizer.addData(new byte[32]);
    assertFalse(paketizer.packetAvailable());
    assertEquals(32, paketizer.pendingData());
  }

  public void testHandshakePacketAvailable() {
    HandshakePacket handshakePacket =
        new HandshakePacket(HashTest.randomHash(), PeerId.createRandomMainlineId());
    EasyMock.expect(
        packetFactory.buildHandshakePacket((byte[]) EasyMock.anyObject(), EasyMock.eq(0), EasyMock.eq(68)))
        .andReturn(handshakePacket);
    EasyMock.replay(packetFactory);

    addHandshakePacket();
    assertTrue(paketizer.packetAvailable());
    assertEquals(0, paketizer.pendingData());
    assertSame(handshakePacket, paketizer.getNextPacket());
    assertFalse(paketizer.packetAvailable());
  }

  private void addHandshakePacket() {
    HandshakePacket packet = new HandshakePacket(HashTest.randomHash(), PeerId.createRandomMainlineId());
    byte[] buffer = new byte[68];
    packet.writeIntoBuffer(buffer, 0);
    paketizer.addData(buffer);
  }

  public void testAddingKeepAlivePacket() {
    HandshakePacket handshakePacket =
        new HandshakePacket(HashTest.randomHash(), PeerId.createRandomMainlineId());
    KeepAlivePacket keepAlivePacket = new KeepAlivePacket();
    EasyMock.expect(
        packetFactory.buildHandshakePacket((byte[]) EasyMock.anyObject(), EasyMock.eq(0), EasyMock.eq(68)))
        .andReturn(handshakePacket);
    EasyMock.expect(
        packetFactory.buildPacket((byte[]) EasyMock.anyObject(), EasyMock.eq(0), EasyMock.eq(4)))
        .andReturn(keepAlivePacket);
    EasyMock.replay(packetFactory);

    addHandshakePacket();
    paketizer.getNextPacket();
    ByteBuffer buffer = ByteBuffer.allocate(100);
    buffer.put((byte) 0x00);
    buffer.put((byte) 0x00);
    buffer.put((byte) 0x00);
    buffer.put((byte) 0x00);
    buffer.put((byte) 0x55);
    buffer.position(0);
    paketizer.addData(buffer, 5);
    assertTrue(paketizer.packetAvailable());
    assertEquals(1, paketizer.pendingData());
    assertEquals(keepAlivePacket, paketizer.getNextPacket());
    assertFalse(paketizer.packetAvailable());
  }

  public void testIncredibleLongPacket() {
    HandshakePacket handshakePacket =
        new HandshakePacket(HashTest.randomHash(), PeerId.createRandomMainlineId());
    EasyMock.expect(
        packetFactory.buildHandshakePacket((byte[]) EasyMock.anyObject(), EasyMock.eq(0), EasyMock.eq(68)))
        .andReturn(handshakePacket);
    EasyMock.replay(packetFactory);

    addHandshakePacket();
    paketizer.getNextPacket();
    ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.put((byte) 0xff);
    buffer.put((byte) 0xff);
    buffer.put((byte) 0xff);
    buffer.put((byte) 0xff);
    buffer.position(0);
    paketizer.addData(buffer, 4);
    try {
      paketizer.packetAvailable();
      fail("should not be callable anymore");
    } catch (IllegalStateException e) {
      // expected
    }
    try {
      paketizer.addData(buffer, 4);
      fail("should not be callable anymore");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  public void testTooLongProtocolName() {
    EasyMock.replay(packetFactory);
    paketizer.addData(new byte[] {120, 0, 0, 0});
    try {
      paketizer.getNextPacket();
      fail("packetizer should be in unusable state after such a long protocol message");
    } catch (IllegalStateException e) {
      // expected
    }
  }
}
