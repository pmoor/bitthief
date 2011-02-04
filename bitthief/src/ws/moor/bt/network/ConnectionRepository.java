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

import org.apache.log4j.Logger;
import ws.moor.bt.downloader.TorrentDownload;
import ws.moor.bt.util.LoggingUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * TODO(pmoor): Javadoc
 */
public class ConnectionRepository {

  private final Map<BitTorrentConnection, ConnectionEntry> incomingConnections =
      new HashMap<BitTorrentConnection, ConnectionEntry>();
  private final Map<BitTorrentConnection, ConnectionEntry> outgoingConnections =
      new HashMap<BitTorrentConnection, ConnectionEntry>();

  private static final Logger logger =
      LoggingUtil.getLogger(ConnectionRepository.class);

  public synchronized void addIncomingConnection(BitTorrentConnection connection) {
    assertIncomingConnection(connection);
    ConnectionEntry entry = new ConnectionEntry(connection);
    incomingConnections.put(connection, entry);
  }

  private void assertIncomingConnection(BitTorrentConnection connection) {
    if (!connection.isInbound()) {
      throw new IllegalArgumentException("should be an incoming connection");
    }
  }

  public int getIncomingConnectionCount() {
    return incomingConnections.size();
  }

  public synchronized void closeConnection(BitTorrentConnection connection) {
    incomingConnections.remove(connection);
    outgoingConnections.remove(connection);
  }

  public synchronized void addOutgoingConnection(BitTorrentConnection connection) {
    assertOutgoingConnection(connection);
    ConnectionEntry entry = new ConnectionEntry(connection);
    outgoingConnections.put(connection, entry);
  }

  private void assertOutgoingConnection(BitTorrentConnection connection) {
    if (!connection.isOutbound()) {
      throw new IllegalArgumentException("should be an outgoing connection");
    }
  }

  public int getOutgoingConnectionCount() {
    return outgoingConnections.size();
  }

  public void startMaintenanceThread(ScheduledExecutorService executor) {
    executor.scheduleWithFixedDelay(new MaintenanceCommand(), 20, 20, TimeUnit.SECONDS);
  }

  private synchronized Set<BitTorrentConnection> getAllConnections() {
    Set<BitTorrentConnection> result = new HashSet<BitTorrentConnection>();
    result.addAll(outgoingConnections.keySet());
    result.addAll(incomingConnections.keySet());
    return result;
  }

  public boolean hasConnectionOpenTo(InetAddress address) {
    if (address == null) {
      return false;
    }
    Set<BitTorrentConnection> connections = getAllConnections();
    for (BitTorrentConnection connection : connections) {
      InetSocketAddress remoteAddress = connection.getRemoteAddress();
      if (remoteAddress != null && address.equals(remoteAddress.getAddress())) {
        return true;
      }
    }
    return false;
  }

  public boolean hasConnectionOpenTo(InetSocketAddress address) {
    if (address == null) {
      return false;
    }
    Set<BitTorrentConnection> connections = getAllConnections();
    for (BitTorrentConnection connection : connections) {
      InetSocketAddress remoteAddress = connection.getRemoteAddress();
      if (remoteAddress != null && address.equals(remoteAddress)) {
        return true;
      }
    }
    return false;
  }

  public Set<BitTorrentConnection> getAllValidConnections(TorrentDownload download) {
    Set<BitTorrentConnection> connections = getAllConnections();
    for (Iterator<BitTorrentConnection> it = connections.iterator(); it.hasNext();) {
      BitTorrentConnection connection = it.next();
      if (connection.getTorrentDownload() != download
          || !connection.isProperlySetUp()
          || !connection.isOpen()) {
        it.remove();
      }
    }
    return connections;
  }

  public void closeAllConnections(TorrentDownload download) {
    Set<BitTorrentConnection> connections = getAllValidConnections(download);
    for (BitTorrentConnection connection : connections) {
      connection.closeBecauseTorrentStops();
    }
  }

  private class MaintenanceCommand implements Runnable {
    public void run() {
      try {
        safeRun();
      } catch (Exception e) {
        logger.error("exception during connection maintenance", e);
      }
    }

    private void safeRun() {
      logger.info("doing connection maintenance");
      Set<BitTorrentConnection> connections = getAllConnections();
      for (BitTorrentConnection connection : connections) {
        connection.doMaintenance();
      }
    }
  }

  private class ConnectionEntry {

    private final BitTorrentConnection connection;

    public ConnectionEntry(BitTorrentConnection connection) {
      this.connection = connection;
    }
  }
}
