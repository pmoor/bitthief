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

import org.apache.log4j.Logger;
import ws.moor.bt.bencoding.BDecoder;
import ws.moor.bt.bencoding.BDictionary;
import ws.moor.bt.bencoding.BEntity;
import ws.moor.bt.bencoding.BInteger;
import ws.moor.bt.bencoding.BList;
import ws.moor.bt.bencoding.BString;
import ws.moor.bt.bencoding.ParseException;
import ws.moor.bt.dht.messages.DHTMessage;
import ws.moor.bt.dht.messages.ErrorMessage;
import ws.moor.bt.dht.messages.QueryMessage;
import ws.moor.bt.torrent.Hash;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.tracker.TrackerResponse;
import ws.moor.bt.util.LoggingUtil;

import java.io.IOException;
import java.net.InetSocketAddress;

public class DHTPacketParser {

  private final BDecoder decoder = new BDecoder();
  private final PacketHandler packetHandler;
  private final DHTSocket socket;

  private final static Logger logger = LoggingUtil.getLogger(DHTPacketParser.class);

  public DHTPacketParser(PacketHandler packetHandler, DHTSocket socket) {
    this.packetHandler = packetHandler;
    this.socket = socket;
  }

  public void parse(byte[] data, InetSocketAddress sender) throws IOException {
    BEntity entity;
    try {
      entity = decoder.decode(data);
    } catch (IOException e) {
      logger.warn("exception during parsing of message", e);
      return;
    } catch (ParseException e) {
      logger.warn("parsing exception", e);
      return;
    }

    if (!(entity instanceof BDictionary)) {
      logger.warn("invalid message format");
      return;
    }
    processDictionary((BDictionary<BEntity>) entity, sender);
  }

  private void processDictionary(BDictionary<BEntity> dictionary, InetSocketAddress sender) throws
      IOException {
    BEntity entity = dictionary.getByString("t");
    if (entity == null || !(entity instanceof BString)) {
      logger.warn("invalid transaction id");
      return;
    }
    TransactionId transactionId = new TransactionId(((BString) entity).getBytes());

    try {
      dispatchToMessageType(dictionary, transactionId, sender);
    } catch (ProtocolError protocolError) {
      ErrorMessage errorMessage =
          new ErrorMessage(transactionId, ErrorMessage.PROTOTOL_ERROR, protocolError.getMessage());
      socket.error(errorMessage, sender);
    }
  }

  private void dispatchToMessageType(BDictionary<BEntity> dictionary, TransactionId transactionId, InetSocketAddress sender) throws
      ProtocolError, IOException {
    BEntity entity;
    entity = dictionary.getByString("y");
    if (entity == null || !(entity instanceof BString)) {
      logger.warn("invalid message type");
      return;
    }
    char messageType = ((BString) entity).toString().charAt(0);

    if (DHTMessage.QUERY_MESSAGE_TYPE == messageType) {
      processQueryMessage(dictionary, transactionId, sender);
    } else if (DHTMessage.RESPONSE_MESSAGE_TYPE == messageType) {
      processResponseMessage(dictionary, transactionId, sender);
    } else if (DHTMessage.ERROR_MESSAGE_TYPE == messageType) {
      logger.info("receiving an error message: " + dictionary);
    } else {
      throw new ProtocolError("unknown message type");
    }
  }

  private void processResponseMessage(BDictionary<BEntity> dictionary, TransactionId transactionId, InetSocketAddress sender)
      throws ProtocolError, IOException {
    BEntity entity = dictionary.getByString("r");
    if (entity == null || !(entity instanceof BDictionary)) {
      throw new ProtocolError("missing reply values");
    }
    BDictionary<BEntity> reply = (BDictionary<BEntity>) entity;

    String queryType = socket.getQueryType(transactionId);
    if (queryType == null) {
      logger.warn("cannot determine query type of transaction id " + transactionId);
      return;
    }

    if (QueryMessage.PING_QUERY.equals(queryType)) {
      processPingReply(reply, transactionId, sender);
    } else if (QueryMessage.FIND_NODE_QUERY.equals(queryType)) {
      processFindNodeReply(reply, transactionId, sender);
    } else if (QueryMessage.GET_PEERS_QUERY.equals(queryType)) {
      processGetPeersReply(reply, transactionId, sender);
    } else if (QueryMessage.ANNOUNCE_PEER_QUERY.equals(queryType)) {

    } else {
      logger.error("unknown query type: " + queryType);
    }
  }

  private void processGetPeersReply(BDictionary<BEntity> reply, TransactionId transactionId, InetSocketAddress sender)
      throws ProtocolError {
    PeerId peerId = extractPeerId(reply);

    BEntity entity = reply.getByString("token");
    if (entity == null || !(entity instanceof BString)) {
      throw new ProtocolError("missing token");
    }
    Token token = new Token(((BString) entity).getBytes());

    entity = reply.getByString("nodes");
    if (entity != null) {
      TrackerResponse.PeerInfo[] nodes;
      if (entity instanceof BString) {
        nodes = nodeBytesToArray(((BString) entity).getBytes());
      } else {
        throw new ProtocolError("invalid nodes type");
      }
      logger.debug("got get_peers reply from " + sender + " (" + peerId + ") and token " + token);
      packetHandler.getPeersReply(sender, transactionId, peerId, token, nodes);
      return;
    }

    entity = reply.getByString("values");
    if (entity == null || !(entity instanceof BList)) {
      throw new ProtocolError("missing values");
    }
    BList<BEntity> list = (BList<BEntity>) entity;

    TrackerResponse.PeerInfo[] peers = listToPeerInfo(list);

    logger.debug("got get_peers reply with matches from " + sender + " (" + peerId + ") and token " + token);
    packetHandler.getPeersReplyWithMatches(sender, transactionId, peerId, token, peers);
  }

  private TrackerResponse.PeerInfo[] listToPeerInfo(BList<BEntity> list) throws ProtocolError {
    TrackerResponse.PeerInfo[] peers = new TrackerResponse.PeerInfo[list.size()];
    int i = 0;
    for (BEntity entry : list) {
      if (!(entry instanceof BString)) {
        throw new ProtocolError("invalid entry format");
      }
      peers[i] = TrackerResponse.PeerInfo.fromCompactForm(((BString) entry).getBytes(), 0);
    }
    return peers;
  }

  private void processFindNodeReply(BDictionary<BEntity> reply, TransactionId transactionId, InetSocketAddress sender)
      throws ProtocolError, IOException {
    PeerId peerId = extractPeerId(reply);

    BEntity entity = reply.getByString("nodes");
    if (entity == null || !(entity instanceof BString)) {
      throw new ProtocolError("missing nodes");
    }
    TrackerResponse.PeerInfo[] nodes = nodeBytesToArray(((BString) entity).getBytes());

    logger.debug("got a find_node reply from " + sender + " (" + peerId + ")");
    packetHandler.findNodeReply(sender, transactionId, peerId, nodes);
  }

  private TrackerResponse.PeerInfo[] nodeBytesToArray(byte[] nodeBytes) throws ProtocolError {
    if (nodeBytes.length % 26 != 0) {
      throw new ProtocolError("nodes must be of length n * 26");
    }

    int numberOfNodes = nodeBytes.length / 26;
    TrackerResponse.PeerInfo[] nodes = new TrackerResponse.PeerInfo[numberOfNodes];
    try {
      for (int i = 0; i < numberOfNodes; i++) {
        nodes[i] = TrackerResponse.PeerInfo.fromCompactLongForm(
            nodeBytes, i * 26);
      }
    } catch (IllegalArgumentException e) {
      throw new ProtocolError("error during parsing of peer info structures");
    }
    return nodes;
  }

  private void processPingReply(BDictionary<BEntity> reply, TransactionId transactionId, InetSocketAddress sender) throws
      ProtocolError {
    PeerId peerId = extractPeerId(reply);
    logger.debug("got a ping reply from " + sender + " (" + peerId + ")");
    packetHandler.pingReply(sender, transactionId, peerId);
  }

  private void processQueryMessage(BDictionary dictionary, TransactionId transactionId, InetSocketAddress sender)
      throws ProtocolError,
      IOException {
    BEntity queryType = dictionary.getByString("q");
    if (queryType == null || !(queryType instanceof BString)) {
      throw new ProtocolError("missing query type");
    }
    String queryTypeString = ((BString) queryType).toString().toLowerCase();

    BEntity queryArguments = dictionary.getByString("a");
    if (queryArguments == null || !(queryArguments instanceof BDictionary)) {
      throw new ProtocolError("missing query arguments");
    }
    BDictionary<BEntity> queryArgumentsDictionary = (BDictionary<BEntity>) queryArguments;

    if (QueryMessage.PING_QUERY.equals(queryTypeString)) {
      processPingQuery(queryArgumentsDictionary, transactionId, sender);
    } else if (QueryMessage.GET_PEERS_QUERY.equals(queryTypeString)) {
      processGetPeersQuery(queryArgumentsDictionary, transactionId, sender);
    } else if (QueryMessage.FIND_NODE_QUERY.equals(queryTypeString)) {
      processFindNodeQuery(queryArgumentsDictionary, transactionId, sender);
    } else if (QueryMessage.ANNOUNCE_PEER_QUERY.equals(queryTypeString)) {
      processAnnouncePeerQuery(queryArgumentsDictionary, transactionId, sender);
    } else {
      throw new ProtocolError("unknown query type");
    }
  }

  private void processAnnouncePeerQuery(BDictionary<BEntity> arguments, TransactionId transactionId,
                                        InetSocketAddress sender) throws ProtocolError, IOException {
    PeerId peerId = extractPeerId(arguments);

    BEntity entity = arguments.getByString("info_hash");
    if (entity == null || !(entity instanceof BString)) {
      throw new ProtocolError("missing info_hash");
    }
    Hash infoHash = Hash.forByteArray(((BString) entity).getBytes());

    entity = arguments.getByString("token");
    if (entity == null || !(entity instanceof BString)) {
      throw new ProtocolError("missing token");
    }
    Token token = new Token(((BString) entity).getBytes());

    entity = arguments.getByString("port");
    if (entity == null || !(entity instanceof BInteger)) {
      throw new ProtocolError("missing port");
    }
    int port = ((BInteger) entity).intValue();

    logger.debug("got announce_peer query from "
        + sender + "(" + peerId + ") for info_hash "
        + infoHash + " with token " + token + " on port " + port);
    packetHandler.announcePeer(sender, transactionId, peerId, infoHash, token, port);
  }

  private void processFindNodeQuery(BDictionary<BEntity> arguments, TransactionId transactionId,
                                    InetSocketAddress sender) throws ProtocolError, IOException {
    PeerId peerId = extractPeerId(arguments);

    BEntity targ = arguments.getByString("target");
    if (targ == null || !(targ instanceof BString)) {
      throw new ProtocolError("missing target");
    }

    PeerId target = new PeerId(((BString) targ).getBytes());

    logger.debug("got find_node query from " + sender + "(" + peerId + ") for target " + target);
    packetHandler.findNode(sender, transactionId, peerId, target);
  }

  private void processGetPeersQuery(BDictionary<BEntity> arguments, TransactionId transactionId,
                                    InetSocketAddress sender) throws ProtocolError, IOException {
    PeerId peerId = extractPeerId(arguments);

    BEntity infoHash = arguments.getByString("info_hash");
    if (infoHash == null || !(infoHash instanceof BString)) {
      throw new ProtocolError("missing info_hash");
    }
    Hash hash = Hash.forByteArray(((BString) infoHash).getBytes());

    logger.debug("got get_peers query from " + sender + "(" + peerId + ") for info_hash " + hash);
    packetHandler.getPeers(sender, transactionId, peerId, hash);
  }

  private void processPingQuery(BDictionary<BEntity> arguments, TransactionId transactionId,
                                InetSocketAddress sender) throws ProtocolError, IOException {
    PeerId peerId = extractPeerId(arguments);
    logger.debug("got ping query from " + sender + "(" + peerId + ")");
    packetHandler.ping(sender, transactionId, peerId);
  }

  private PeerId extractPeerId(BDictionary<BEntity> arguments) throws ProtocolError {
    BEntity id = arguments.getByString("id");
    if (id == null || !(id instanceof BString)) {
      throw new ProtocolError("missing id");
    }
    return new PeerId(((BString) id).getBytes());
  }
}
