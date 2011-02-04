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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class ConnectionInitiator {

  private final TorrentDownload torrentDownload;

  private final List<EventHandler> pendingConnects =
      Collections.synchronizedList(new ArrayList<EventHandler>());

  private static final int MAX_NUMBER_OF_PENDING_CONNECTS = 64;

  private static final Logger logger = LoggingUtil.getLogger(ConnectionInitiator.class);

  public ConnectionInitiator(TorrentDownload torrentDownload) {
    this.torrentDownload = torrentDownload;
  }

  public synchronized void maintenance() {
    int numberOfAvailableSpots = MAX_NUMBER_OF_PENDING_CONNECTS - pendingConnects.size();
    if (numberOfAvailableSpots < 1) {
      return;
    }

    List<InetSocketAddress> addresses =
        torrentDownload.getPeerManager().getConnectCandidates(Math.max(4, numberOfAvailableSpots));
    for (InetSocketAddress address : addresses) {
      try {
        openConnectionTo(address);
      } catch (IOException e) {
        logger.warn("error while trying to connect to " + address, e);
      }
    }
  }

  private synchronized void openConnectionTo(InetSocketAddress remoteAddress) throws IOException {
    if (hasAlreadyAnOpenConnection(remoteAddress)) {
      return;
    }
    logger.info("trying to start a connection to " + remoteAddress);
    SocketChannel channel = SocketChannel.open();
    channel.configureBlocking(false);
    EventHandler eventHandler = new EventHandler(channel);
    torrentDownload.getEnvironment().getNetworkManager().registerForConnectableEvents(eventHandler, channel);
    pendingConnects.add(eventHandler);
    channel.connect(remoteAddress);
    torrentDownload.getCounterRepository().getCounter(
        "network.connections.pending").set(pendingConnects.size());
  }

  private boolean hasAlreadyAnOpenConnection(InetSocketAddress remoteAddress) {
    return torrentDownload.getEnvironment().getConnectionRepository().hasConnectionOpenTo(remoteAddress.getAddress());
  }

  private synchronized void removeEntry(EventHandler handler) {
    pendingConnects.remove(handler);
    torrentDownload.getCounterRepository().getCounter(
        "network.connections.pending").set(pendingConnects.size());
  }

  private class EventHandler extends AbstractSocketEventHandler {

    private final SocketChannel channel;

    public EventHandler(SocketChannel channel) {
      this.channel = channel;
    }

    public void becomesConnectable(SelectionKey key) throws IOException {
      logger.trace("we've become connectable");
      SelectableChannel keyChannel = key.channel();
      if (keyChannel != channel) {
        logger.error("expected a different channel: " + channel + " instead of " + keyChannel);
        removeEntry(this);
        return;
      }

      try {
        if (channel.finishConnect()) {
          logger.debug("finish connect succeeded");
          key.interestOps(0);
          BitTorrentConnection.outgoingConnection(channel, torrentDownload);
        }
      } catch (IOException e) {
        channel.close();
      } catch (NoConnectionPendingException e) {
        logger.warn("could not finish the connection", e);
      } finally {
        removeEntry(this);
      }
    }
  }
}
