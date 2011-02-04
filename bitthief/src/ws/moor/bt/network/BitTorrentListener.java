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
import ws.moor.bt.Environment;
import ws.moor.bt.util.LoggingUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * TODO(pmoor): Javadoc
 */
public class BitTorrentListener extends AbstractSocketEventHandler {

  private final static Logger logger = LoggingUtil.getLogger(BitTorrentListener.class);

  private final ServerSocketChannel channel;
  private final Environment environment;
  private final int port;

  public BitTorrentListener(Environment environment, int port) throws IOException {
    this.environment = environment;
    this.port = port;

    logger.info("creating new bittorrent listener on tcp port " + port);
    channel = ServerSocketChannel.open();
    channel.configureBlocking(false);
    ServerSocket socket = channel.socket();
    socket.bind(new InetSocketAddress(port));
    environment.getNetworkManager().registerForAcceptEvents(this, channel);
  }

  public void becomesAcceptable(SelectionKey key) throws IOException {
    SocketChannel clientChannel = channel.accept();
    clientChannel.configureBlocking(false);
    BitTorrentConnection.incomingConnection(clientChannel, environment);
  }

  public int getPort() {
    return port;
  }
}
