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

package ws.moor.bt.simulation;

import org.apache.log4j.Logger;
import ws.moor.bt.util.LoggingUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * TODO(pmoor): Javadoc
 */
public class Node implements NetworkNode, RarestFirstDeterminer.TupleInformationProvider {

  protected final SimulationNetwork network;

  private final long peerId;

  private final RateLimiter outgoingRateLimiter;
  private final RateLimiter incomingRateLimiter;

  protected NodeMode mode;

  protected final List<Connection> connections =
      new ArrayList<Connection>();

  protected NextTupleDeterminer determiner =
      new RarestFirstDeterminer(this);

  protected Set<Tuple> availableTuples =
      new HashSet<Tuple>();

  private final int[] blockFrequency;

  private Map<Tuple, Integer> tupleCounts =
      new HashMap<Tuple, Integer>();

  private TupleAnnounceStrategy tupleAnnounceStrategy =
      new OrdinaryAnnounceStrategy(this);

  private boolean online = true;

  private final boolean firewalled;

  private final long startTime;
  private static final Logger logger = LoggingUtil.getLogger(Node.class);
  private static final int BLOCK_COUNT = 1024;
  private static final double FIREWALLED_PROBABILITY = 0.1;

  public Node(SimulationNetwork simulationNetwork, int peerId) {
    this.network = simulationNetwork;
    this.peerId = peerId;
    this.firewalled = Math.random() > (1.0 - FIREWALLED_PROBABILITY);
    outgoingRateLimiter = constructOutgoingRateLimiter();
    incomingRateLimiter = constructIncomingRateLimiter(outgoingRateLimiter);

    logger.info("node " + peerId + ": outgoing " + outgoingRateLimiter + ", incoming " + incomingRateLimiter
        + ", " + (isFirewalled() ? "" : "not ") + "firewalled");
    mode = NodeMode.LEECHER;
    blockFrequency = new int[getBlockCount()];
    network.getAgenda().scheduleDelta(new FindNewPeers(), 5 * 1000);

    startTime = network.getTime();
  }

  protected RateLimiter constructOutgoingRateLimiter() {
    Random rnd = new Random();
    int rate = rnd.nextInt(128) + 4;
    int delay = rnd.nextInt(100) + 10;
    return new RateLimiter(rate, delay, network.getAgenda().getTimeSource());
  }

  protected RateLimiter constructIncomingRateLimiter(RateLimiter outgoingRateLimiter) {
    Random rnd = new Random();
    int rate = (int) ((10 * rnd.nextDouble() + 1) * outgoingRateLimiter.getRate());
    int delay = rnd.nextInt(100) + 10;
    return new RateLimiter(rate, delay, network.getAgenda().getTimeSource());
  }

  public long getId() {
    return peerId;
  }

  public Set<NetworkNode> getNeighbours() {
    Set<NetworkNode> nodes = new HashSet<NetworkNode>();
    for (Connection connection : connections) {
      nodes.add(connection.getRemote());
    }
    return nodes;
  }

  public void newIncomingConnection(NetworkSocket socket) {
    if (getNeighbours().contains(socket.getRemote())) {
      // already connected
      socket.close();
      return;
    }
    if (!acceptIncomingConnection()) {
      socket.close();
      return;
    }
    if (!online) {
      socket.close();
      return;
    }
    createConnection(socket);
  }

  protected boolean acceptIncomingConnection() {
    return connections.size() < 10;
  }

  public void newOutgoingConnection(NetworkSocket socket) {
    createConnection(socket);
  }

  public boolean isOnline() {
    return online;
  }

  public boolean isFirewalled() {
    return firewalled;
  }

  public double getCompletionRate() {
    return Math.min(1.0, (double) availableTuples.size() / getBlockCount());
  }

  public RateLimiter getOutgoingRateLimiter() {
    return outgoingRateLimiter;
  }

  public RateLimiter getIncomingRateLimiter() {
    return incomingRateLimiter;
  }

  protected void createConnection(NetworkSocket socket) {
    Connection connection = new Connection(this, socket, network.getAgenda().getTimeSource());
    connections.add(connection);
    network.getRepository().getCounter("connections", Long.toString(peerId)).set(connections.size());
  }

  private void findNewPeers() {
    if (removeAnOldConnection()) {
      Connection minConnection = null;
      long maxAge = Long.MIN_VALUE;
      for (Connection connection : connections) {
        long age = connection.timeSinceLastReceivedTuple();
        if (age > maxAge) {
          maxAge = age;
          minConnection = connection;
        }
      }
      if (minConnection != null) {
        minConnection.closeConnection();
        closedConnection(minConnection);
      }
    }
    NetworkNode otherNode = network.pickRandomNode();
    if (otherNode == this) {
      return;
    }
    network.connect(this, otherNode);
  }

  protected boolean removeAnOldConnection() {
    return connections.size() > 8;
  }

  public void closedConnection(Connection connection) {
    connections.remove(connection);
    network.getRepository().getCounter("connections", Long.toString(peerId)).set(connections.size());
  }

  public Set<Tuple> getAvailableTuples() {
    return availableTuples;
  }

  public int getBlockCount() {
    return BLOCK_COUNT;
  }

  public NextTupleDeterminer getNextTupleDeterminer() {
    return determiner;
  }

  public void addTuple(Tuple tuple) {
    if (availableTuples.contains(tuple)) {
      return;
    }
    availableTuples.add(tuple);
    network.getRepository().getCounter("tuples.available", Long.toString(peerId)).set(availableTuples.size());
    updateBlockFrequency(tuple);
    if (availableTuples.size() % 128 == 0) {
      logger.info("node " + peerId + " has " + availableTuples.size() + " tuples");
    }
    for (Connection connection : connections) {
      connection.newTupleAvailable(tuple);
    }
    if (availableTuples.size() >= getBlockCount() && everyBlockAvailable()) {
      goOffline();
    }
  }

  private boolean everyBlockAvailable() {
    for (int i : blockFrequency) {
      if (i < 1) {
        return false;
      }
    }
    return true;
  }

  protected void goOffline() {
    if (online) {
      long onlineTime = network.getTime() - startTime;
      int rate = outgoingRateLimiter.getRate();
      logger.info("node " + peerId + " is going offline after " + onlineTime + " seconds with rate " + rate);
      for (Connection con : connections) {
        con.closeConnection();
      }
      connections.clear();
      network.getRepository().getCounter("connections", Long.toString(peerId)).set(0);
      online = false;
    }
  }

  protected void updateBlockFrequency(Tuple tuple) {
    for (int i = 0; i < tuple.size(); i++) {
      blockFrequency[tuple.get(i)]++;
    }
  }

  public String toString() {
    return String.valueOf(peerId);
  }

  public int[] getBlockFrequencies() {
    return blockFrequency;
  }

  public NodeMode getMode() {
    return mode;
  }

  public boolean isSeeder() {
   return getMode() == NodeMode.SEEDER;
  }

  public boolean isLeecher() {
    return getMode() == NodeMode.LEECHER;
  }

  public TupleAnnounceStrategy getTupleAnnounceStrategy() {
    return tupleAnnounceStrategy;
  }

  public void newTupleSeen(Tuple tuple) {
    Integer count = tupleCounts.get(tuple);
    count = count == null ? 1 : count + 1;
    tupleCounts.put(tuple, count);
  }

  public Map<Tuple, Integer> getTupleCounts() {
    return tupleCounts;
  }

  private class FindNewPeers extends Event {
    public void execute() {
      findNewPeers();
    }

    public boolean doRun() {
      return online;
    }

    public long getRescheduleInterval() {
      return online ? (long) (Math.pow(connections.size(), 1.5) + 15) * 1000 : 0;
    }
  }
}
