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

package ws.moor.bt.dht;

import ws.moor.bt.torrent.Hash;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.tracker.TrackerResponse;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * TODO(pmoor): Javadoc
 */
public interface PacketHandler {
  void findNode(InetSocketAddress sender, TransactionId transactionId, PeerId id, PeerId target) throws IOException;

  void findNodeReply(InetSocketAddress sender, TransactionId transactionId, PeerId id,
                     TrackerResponse.PeerInfo[] nodes) throws IOException;

  void getPeers(InetSocketAddress sender, TransactionId transactionId, PeerId id, Hash infoHash) throws IOException;

  void getPeersReply(InetSocketAddress sender, TransactionId transactionId, PeerId id, Token token,
                     TrackerResponse.PeerInfo[] nodes);

  void getPeersReplyWithMatches(InetSocketAddress sender, TransactionId transactionId, PeerId id, Token token,
                                TrackerResponse.PeerInfo[] matches);

  void ping(InetSocketAddress sender, TransactionId transactionId, PeerId id) throws IOException;

  void pingReply(InetSocketAddress sender, TransactionId transactionId, PeerId id);

  void announcePeer(InetSocketAddress sender, TransactionId transactionId, PeerId id, Hash infoHash, Token token,
                    int port) throws IOException;
}
