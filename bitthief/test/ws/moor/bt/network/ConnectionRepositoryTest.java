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
import ws.moor.bt.downloader.TorrentDownload;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.ExtendedTestCase;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * TODO(pmoor): Javadoc
 */
public class ConnectionRepositoryTest extends ExtendedTestCase {

  public void testAddIncoming() {
    ConnectionRepository repository = createConnectionRepository();

    BitTorrentConnection connection = EasyMock.createMock(BitTorrentConnection.class);
    EasyMock.expect(connection.isInbound()).andReturn(true).anyTimes();
    EasyMock.replay(connection);

    repository.addIncomingConnection(connection);

    assertEquals(1, repository.getIncomingConnectionCount());

    repository.closeConnection(connection);

    assertEquals(0, repository.getIncomingConnectionCount());

    EasyMock.verify(connection);
  }

  public void testAddOutgoing() {
    ConnectionRepository repository = createConnectionRepository();

    BitTorrentConnection connection = EasyMock.createMock(BitTorrentConnection.class);
    EasyMock.expect(connection.isOutbound()).andReturn(true).anyTimes();
    EasyMock.replay(connection);

    repository.addOutgoingConnection(connection);

    assertEquals(1, repository.getOutgoingConnectionCount());

    repository.closeConnection(connection);

    assertEquals(0, repository.getOutgoingConnectionCount());

    EasyMock.verify(connection);
  }

  public void testHasConnectionOpen() throws UnknownHostException {
    ConnectionRepository repository = createConnectionRepository();
    InetAddress inetAddress =
        InetAddress.getByAddress(ByteUtil.newByteArray(127, 0, 0, 3));
    InetSocketAddress remoteAddress = new InetSocketAddress(inetAddress, 7770);

    BitTorrentConnection connection = EasyMock.createMock(BitTorrentConnection.class);
    EasyMock.expect(connection.isOutbound()).andReturn(true).anyTimes();
    EasyMock.expect(connection.getRemoteAddress()).andReturn(remoteAddress).anyTimes();
    EasyMock.replay(connection);

    repository.addOutgoingConnection(connection);

    assertTrue(repository.hasConnectionOpenTo(inetAddress));
    assertTrue(repository.hasConnectionOpenTo(remoteAddress));

    repository.closeConnection(connection);

    assertFalse(repository.hasConnectionOpenTo(inetAddress));
    assertFalse(repository.hasConnectionOpenTo(remoteAddress));

    EasyMock.verify(connection);
  }

  private ConnectionRepository createConnectionRepository() {
    return new ConnectionRepository();
  }

  public void testNullQuery() {
    ConnectionRepository repository = createConnectionRepository();
    assertFalse(repository.hasConnectionOpenTo((InetAddress) null));
    assertFalse(repository.hasConnectionOpenTo((InetSocketAddress) null));
  }

  public void testHasConnectionOpenToWorksForClosedConnections() throws UnknownHostException {
    ConnectionRepository repository = createConnectionRepository();
    InetAddress inetAddress =
        InetAddress.getByAddress(ByteUtil.newByteArray(127, 0, 0, 3));
    InetSocketAddress remoteAddress = new InetSocketAddress(inetAddress, 7770);

    BitTorrentConnection connection = EasyMock.createMock(BitTorrentConnection.class);
    EasyMock.expect(connection.isOutbound()).andReturn(true).anyTimes();
    EasyMock.expect(connection.getRemoteAddress()).andReturn(null).anyTimes();
    EasyMock.replay(connection);

    repository.addOutgoingConnection(connection);

    assertFalse(repository.hasConnectionOpenTo(inetAddress));
    assertFalse(repository.hasConnectionOpenTo(remoteAddress));

    repository.closeConnection(connection);

    assertFalse(repository.hasConnectionOpenTo(inetAddress));
    assertFalse(repository.hasConnectionOpenTo(remoteAddress));

    EasyMock.verify(connection);
  }

  public void testConnectionsForTorrentDownload() {
    TorrentDownload downloadA = EasyMock.createMock(TorrentDownload.class);
    EasyMock.replay(downloadA);
    TorrentDownload downloadB = EasyMock.createMock(TorrentDownload.class);
    EasyMock.replay(downloadB);

    BitTorrentConnection[] connections = new BitTorrentConnection[8];
    connections[0] = createConnection(randomInetSocketAddress(), downloadA, true);
    connections[1] = createConnection(randomInetSocketAddress(), downloadA, true);
    connections[2] = createConnection(randomInetSocketAddress(), downloadA, false);
    connections[3] = createConnection(randomInetSocketAddress(), downloadA, false);
    connections[4] = createConnection(randomInetSocketAddress(), downloadB, false);
    connections[5] = createConnection(randomInetSocketAddress(), downloadB, false);
    connections[6] = createConnection(randomInetSocketAddress(), downloadB, true);
    connections[7] = createConnection(randomInetSocketAddress(), downloadB, true);

    ConnectionRepository repository = createConnectionRepository();
    for (BitTorrentConnection connection : connections) {
      repository.addOutgoingConnection(connection);
    }

    Set<BitTorrentConnection> validConnections = repository.getAllValidConnections(downloadA);
    assertEquals(2, validConnections.size());
    assertContains(connections[0], validConnections);
    assertContains(connections[1], validConnections);

    validConnections = repository.getAllValidConnections(downloadB);
    assertEquals(2, validConnections.size());
    assertContains(connections[6], validConnections);
    assertContains(connections[7], validConnections);

    for (BitTorrentConnection connection : connections) {
      EasyMock.verify(connection);
    }
    EasyMock.verify(downloadA);
    EasyMock.verify(downloadB);
  }

  private BitTorrentConnection createConnection(InetSocketAddress remoteAddress, TorrentDownload torrentDownload,
                                                boolean isOpen) {
    BitTorrentConnection connection = EasyMock.createMock(BitTorrentConnection.class);
    EasyMock.expect(connection.isOutbound()).andReturn(true).anyTimes();
    EasyMock.expect(connection.getRemoteAddress()).andReturn(remoteAddress).anyTimes();
    EasyMock.expect(connection.getTorrentDownload()).andReturn(torrentDownload).anyTimes();
    EasyMock.expect(connection.isOpen()).andReturn(isOpen).anyTimes();
    EasyMock.expect(connection.isProperlySetUp()).andReturn(isOpen).anyTimes();
    EasyMock.replay(connection);
    return connection;
  }
}
