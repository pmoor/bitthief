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
import ws.moor.bt.Environment;
import ws.moor.bt.dht.messages.ErrorMessage;
import ws.moor.bt.dht.messages.QueryMessage;
import ws.moor.bt.dht.messages.ResponseMessage;
import ws.moor.bt.stats.CounterRepository;
import ws.moor.bt.torrent.Hash;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.tracker.TrackerResponse;
import ws.moor.bt.util.LoggingUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TODO(pmoor): Javadoc
 */
public class DHTracker implements PacketHandler, Pinger, NodeFinder {

  private final Environment environment;
  private final int port;
  private final DHTSocket socket;
  private final PeerId ourId;
  private final RoutingTable table;
  private final AnnounceStore announces;
  private final PeerSpider spider;

  private static final Logger logger = LoggingUtil.getLogger(DHTracker.class);

  public DHTracker(Environment environment, TrackerState state) throws IOException {
    this.environment = environment;
    this.port = state.getListeningPort();
    this.ourId = state.getOurId();
    logger.info("starting tracker on port " + port + " with id " + ourId);

    table = new RoutingTable(ourId, this, environment.getCounterRepository());
    announces = new AnnounceStore(8192, 512, environment.getCounterRepository());
    spider = new PeerSpider(ourId, this, environment.getCounterRepository());

    for (TrackerResponse.PeerInfo info : state.getPeers()) {
      spider.heardAbout(info);
    }

    socket = new DHTSocket(environment, this, port);
    environment.getScheduledExecutor().scheduleWithFixedDelay(
        new Maintenance(), 20, 20, TimeUnit.SECONDS);
  }

  public void findNode(InetSocketAddress sender, TransactionId transactionId, PeerId id, PeerId target) throws IOException {
    TrackerResponse.PeerInfo[] nodes = table.getNodesCloseTo(target, 8);
    ResponseMessage message = ResponseMessage.constructFindNodesReply(transactionId, ourId, nodes);
    socket.respond(message, sender);
    spider.heardAbout(new TrackerResponse.PeerInfo(id, sender));
    getCounterRepository().getCounter("dht.findnode.in").increase(1);
  }

  private CounterRepository getCounterRepository() {
    return environment.getCounterRepository();
  }

  public void findNodeReply(InetSocketAddress sender, TransactionId transactionId, PeerId id,
                            TrackerResponse.PeerInfo[] nodes) throws IOException {
    gotReplyFromPeer(id, sender);
    for (TrackerResponse.PeerInfo info : nodes) {
      spider.heardAbout(info);
    }
  }

  public void getPeers(InetSocketAddress sender, TransactionId transactionId, PeerId id, Hash infoHash) throws IOException {
    PeerId hashId = new PeerId(infoHash.getBytes());
    Token token = Token.createForAddress(sender);
    ResponseMessage message = null;
    TrackerResponse.PeerInfo[] peers = announces.getPeers(infoHash, 256);
    if (peers != null) {
      getCounterRepository().getCounter("dht.getpeers.matches").increase(1);
      message = ResponseMessage.constructGetPeersReply(transactionId, ourId, token, peers);
    } else {
      TrackerResponse.PeerInfo[] nodes = table.getNodesCloseTo(hashId, 8);
      message = ResponseMessage.constructGetPeersReplyNoPeers(transactionId, ourId, token, nodes);
    }
    socket.respond(message, sender);
    spider.heardAbout(new TrackerResponse.PeerInfo(id, sender));
    getCounterRepository().getCounter("dht.getpeers.in").increase(1);
  }

  public void getPeersReply(InetSocketAddress sender, TransactionId transactionId, PeerId id, Token token,
                            TrackerResponse.PeerInfo[] nodes) {
    gotReplyFromPeer(id, sender);
    for (TrackerResponse.PeerInfo info : nodes) {
      spider.heardAbout(info);
    }
  }

  public void getPeersReplyWithMatches(InetSocketAddress sender, TransactionId transactionId, PeerId id, Token token,
                                       TrackerResponse.PeerInfo[] matches) {
    gotReplyFromPeer(id, sender);
  }

  public void ping(InetSocketAddress sender, TransactionId transactionId, PeerId id) throws IOException {
    ResponseMessage message = ResponseMessage.constructPingReply(transactionId, ourId);
    socket.respond(message, sender);
    spider.heardAbout(new TrackerResponse.PeerInfo(id, sender));
    getCounterRepository().getCounter("dht.ping.in").increase(1);
  }

  public void pingReply(InetSocketAddress sender, TransactionId transactionId, PeerId id) {
    gotReplyFromPeer(id, sender);
  }

  public void announcePeer(InetSocketAddress sender, TransactionId transactionId,
                           PeerId id, Hash infoHash, Token token,
                           int port) throws IOException {
    if (!Token.isValid(token, sender)) {
      logger.info("invalid token provided by " + sender);
      ErrorMessage error =
          new ErrorMessage(transactionId, ErrorMessage.PROTOTOL_ERROR, "invalid token");
      socket.error(error, sender);
      getCounterRepository().getCounter("dht.announce.invalidtoken").increase(1);
      return;
    }
    announces.announce(infoHash, new InetSocketAddress(sender.getAddress(), sender.getPort()));
    ResponseMessage message = ResponseMessage.constructPingReply(transactionId, ourId);
    socket.respond(message, sender);
    spider.heardAbout(new TrackerResponse.PeerInfo(id, sender));
    getCounterRepository().getCounter("dht.announce.in").increase(1);
  }


  private void gotReplyFromPeer(PeerId id, InetSocketAddress sender) {
    TrackerResponse.PeerInfo info = new TrackerResponse.PeerInfo(id, sender);
    table.addNode(info);
  }

  public void ping(InetSocketAddress address) {
    QueryMessage message =
        QueryMessage.constructPingQuery(TransactionId.createRandom(), ourId);
    try {
      socket.query(message, address);
      getCounterRepository().getCounter("dht.ping.out").increase(1);
    } catch (IOException e) {
      logger.error("exception during ping of " + address, e);
    }
  }

  public void findNode(InetSocketAddress address, PeerId target) {
    logger.trace("sending finde node query to " + target + " at " + address);
    QueryMessage message =
        QueryMessage.constructFindNodeQuery(TransactionId.createRandom(), ourId, target);
    try {
      socket.query(message, address);
      getCounterRepository().getCounter("dht.findnode.out").increase(1);
    } catch (IOException e) {
      logger.error("exception while sending finde node to " + address, e);
    }
  }

  public RoutingTable getRoutingTable() {
    return table;
  }

  public PeerId getPeerId() {
    return ourId;
  }

  public int getPort() {
    return port;
  }

  private class Maintenance implements Runnable {

    public void run() {
      try {
        safeRun();
      } catch (Exception e) {
        logger.error("exception during DHT maintenance", e);
      }
    }

    private void safeRun() {
      PeerId closeByAddress = ourId.flipLastBit();
      for (TrackerResponse.PeerInfo info : getRandomKnownPeersSubset(8)) {
        findNode(info.getSocketAddress(), closeByAddress);
      }

      table.maintain();
      spider.maintain();
      announces.maintain();
    }

    private List<TrackerResponse.PeerInfo> getRandomKnownPeersSubset(int count) {
      List<TrackerResponse.PeerInfo> result = new ArrayList<TrackerResponse.PeerInfo>(count);
      result.addAll(Arrays.asList(table.getNodesCloseTo(PeerId.createRandom(), count * 2)));
      result.addAll(Arrays.asList(table.getNodesCloseTo(ourId, count * 4)));
      Collections.shuffle(result);
      return result.subList(0, Math.min(result.size(), count));
    }
  }
}
