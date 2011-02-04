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
import ws.moor.bt.util.LoggingUtil;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

/**
 * TODO(pmoor): Javadoc
 */
public class NetworkManager implements Runnable {

  private static Logger logger = LoggingUtil.getLogger(NetworkManager.class);

  private final Selector selector;

  public NetworkManager() throws IOException {
    selector = Selector.open();
    new Thread(this, "NetworkManager").start();
  }

  public void run() {
    while (true) {
      try {
        mainloop();
      } catch (Exception e) {
        logger.error("an exception got thrown in mainloop", e);
      }
    }
  }

  private void mainloop() throws IOException {
    if (selector.select(1000) <= 0) {
      logger.debug("select returned <=0");
      Thread.yield();
      return;
    }
    Set<SelectionKey> selectionKeys = selector.selectedKeys();
    logger.trace("selection keys: " + selectionKeys);
    for (SelectionKey key : selectionKeys) {
      try {
        SocketEventHandler handler = (SocketEventHandler) key.attachment();

        if (key.isValid() && key.isAcceptable()) {
          logger.trace("handling acceptable operation");
          handler.becomesAcceptable(key);
        }

        if (key.isValid() && key.isReadable()) {
          logger.trace("handling readable operation");
          handler.becomesReadable(key);
        }

        if (key.isValid() && key.isWritable()) {
          logger.trace("handling writable operation");
          handler.becomesWritable(key);
        }

        if (key.isValid() && key.isConnectable()) {
          logger.trace("handling connectable operation");
          handler.becomesConnectable(key);
        }
      } catch (IOException e) {
        logger.warn("handler threw an exception", e);
      }
    }
    selector.selectedKeys().clear();
  }

  public void registerForWriteEvents(SocketEventHandler socket, SelectableChannel channel) throws ClosedChannelException {
    logger.trace("registering channel for write events");
    addInterestedOps(socket, channel, SelectionKey.OP_WRITE);
  }

  public void unregisterForWriteEvents(SocketEventHandler socket, SelectableChannel channel) throws ClosedChannelException {
    logger.trace("un-registering channel for write events");
    removeInterestedOps(socket, channel, SelectionKey.OP_WRITE);
  }

  public void registerForReadEvents(SocketEventHandler socket, SelectableChannel channel) throws ClosedChannelException {
    logger.trace("registering channel for read events");
    addInterestedOps(socket, channel, SelectionKey.OP_READ);
  }

  public void registerForAcceptEvents(SocketEventHandler socket, SelectableChannel channel) throws ClosedChannelException {
    logger.trace("registering channel for accept events");
    addInterestedOps(socket, channel, SelectionKey.OP_ACCEPT);
  }

  public void registerForConnectableEvents(SocketEventHandler socket, SelectableChannel channel) throws
      ClosedChannelException {
    logger.trace("registering channel for connect events");
    addInterestedOps(socket, channel, SelectionKey.OP_CONNECT);
  }

  private synchronized void removeInterestedOps(SocketEventHandler socket, SelectableChannel channel, int operations) throws
      ClosedChannelException {
    SelectionKey key = getSelectionKey(socket, channel);
    key.interestOps(key.interestOps() & ~operations);
  }

  private synchronized void addInterestedOps(SocketEventHandler socket, SelectableChannel channel, int operation) throws
      ClosedChannelException {
    SelectionKey key = getSelectionKey(socket, channel);
    key.interestOps(key.interestOps() | operation);
  }

  private SelectionKey getSelectionKey(SocketEventHandler socket, SelectableChannel channel) throws ClosedChannelException {
    SelectionKey key = channel.keyFor(selector);
    if (key == null) {
      return channel.register(selector, 0, socket);
    }
    key.attach(socket);
    return key;
  }
}
