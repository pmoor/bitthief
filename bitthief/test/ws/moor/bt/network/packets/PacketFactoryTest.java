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
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class PacketFactoryTest extends ExtendedTestCase {

  private PacketFactory factory;

  public void setUp() throws Exception {
    super.setUp();
    factory = new PacketFactory();
  }

  public void testChokePacket() {
    byte[] buffer = ByteUtil.newByteArray(0, 0, 0, 1, 0);
    ChokePacket packet = new ChokePacket();

    PacketConstructor constructor = EasyMock.createMock(PacketConstructor.class);
    EasyMock.expect(constructor.getId()).andReturn(ChokePacket.ID);
    EasyMock.expect(constructor.constructPacket(buffer, 5, 0)).andReturn(packet);
    EasyMock.replay(constructor);

    factory.addConstructor(constructor);
    assertSame(packet, factory.buildPacket(buffer, 0, 5));

    EasyMock.verify(constructor);
  }

  public void testKeepAlivePacket() {
    byte[] buffer = ByteUtil.newByteArray(55, 0, 0, 0, 0, 77);
    KeepAlivePacket packet = new KeepAlivePacket();

    PacketConstructor<KeepAlivePacket> constructor = EasyMock.createMock(PacketConstructor.class);
    EasyMock.expect(constructor.constructPacket(buffer, 5, 0)).andReturn(packet);
    EasyMock.replay(constructor);

    factory.setKeepAliveConstructor(constructor);
    assertSame(packet, factory.buildPacket(buffer, 1, 5));

    EasyMock.verify(constructor);
  }

  public void testHandshakePacket() {
    byte[] buffer = new byte[70];
    HandshakePacket packet = new HandshakePacket(HashTest.randomHash(), PeerId.createRandomMainlineId());

    PacketConstructor<HandshakePacket> constructor = EasyMock.createMock(PacketConstructor.class);
    EasyMock.expect(constructor.constructPacket(buffer, 1, 68)).andReturn(packet);
    EasyMock.replay(constructor);

    factory.setHandshakeConstructor(constructor);
    assertSame(packet, factory.buildHandshakePacket(buffer, 1, 68));

    EasyMock.verify(constructor);
  }

  public void testAnnounceWrongSize() {
    try {
      factory.buildPacket(ByteUtil.newByteArray(0, 0, 0, 0), 3, 4);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testLessThanMinimal() {
    try {
      factory.buildPacket(ByteUtil.newByteArray(33, 0, 0, 0, 0), 1, 2);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testTooShort() {
    try {
      factory.buildPacket(ByteUtil.newByteArray(0, 0, 0, 50, 0), 0, 5);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testSerializePacket() {

    byte[] buffer = new byte[128];
    Packet packet = EasyMock.createMock(Packet.class);
    EasyMock.expect(packet.getPayloadLength()).andReturn(8).anyTimes();
    EasyMock.expect(packet.getId()).andReturn(42);
    EasyMock.expect(packet.writeIntoBuffer(buffer, 5)).andReturn(13);
    EasyMock.replay(packet);

    factory.serializePacket(packet, buffer, 0);
    assertEquals(0, buffer[0]);
    assertEquals(0, buffer[1]);
    assertEquals(0, buffer[2]);
    assertEquals(9, buffer[3]);
    assertEquals(42, buffer[4]);
  }

  public void testSerializeHandshakePacket() {
    Hash hash = HashTest.randomHash();
    PeerId id = PeerId.createRandomMainlineId();
    HandshakePacket packet = new HandshakePacket(hash, id);
    byte[] buffer = new byte[68];
    factory.serializePacket(packet, buffer, 0);
    byte[] expected = new byte[68];
    expected[0] = 19;
    System.arraycopy(HandshakePacket.BITTORRENT_PROTOCOL.getBytes(), 0, expected, 1, 19);
    System.arraycopy(hash.getBytes(), 0, expected, 28, 20);
    System.arraycopy(id.getBytes(), 0, expected, 48, 20);
    assertArrayEquals(expected, buffer);
  }

  public void testSerializeKeepAlivePacket() {
    KeepAlivePacket packet = new KeepAlivePacket();
    byte[] buffer = ByteUtil.newByteArray(77, 77, 77, 77, 77, 77, 77, 77);
    factory.serializePacket(packet, buffer, 2);
    assertArrayEquals(ByteUtil.newByteArray(77, 77, 0, 0, 0, 0, 77, 77), buffer);
  }

  public void testSerializeInShortBuffer() {
    try {
      factory.serializePacket(new InterestedPacket(), new byte[4], 0);
      fail("should fail");
    } catch (Exception e) {
      // expected
    }
  }

  public void testExpectedBytesOnWire() {
    assertEquals(4, factory.getExpectedBytesOnWire(new KeepAlivePacket()));
    assertEquals(68, factory.getExpectedBytesOnWire(
        new HandshakePacket(HashTest.randomHash(), PeerId.createRandomMainlineId())));
    assertEquals(17, factory.getExpectedBytesOnWire(new CancelPacket(1, 2, 3)));
  }
}
