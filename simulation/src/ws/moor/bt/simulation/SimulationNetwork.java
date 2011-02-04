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
import ws.moor.bt.stats.CounterRepository;
import ws.moor.bt.util.CollectionUtils;
import ws.moor.bt.util.ExtendedRandom;
import ws.moor.bt.util.LoggingUtil;
import ws.moor.bt.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * TODO(pmoor): Javadoc
 */
public class SimulationNetwork {

  private List<NetworkNode> nodes = new ArrayList<NetworkNode>();

  private Agenda agenda;

  private int nodeIds = 1;

  private static final Logger logger = LoggingUtil.getLogger(SimulationNetwork.class);

  public SimulationNetwork() {
    agenda = new Agenda();
    agenda.schedule(new NewNodeJoinsEvent(), 0);
    agenda.schedule(new CleanupEvent(), 0);
    agenda.schedule(new TimeLoggingEvent(), 0);
    agenda.run();
  }

  public Agenda getAgenda() {
    return agenda;
  }

  public long getTime() {
    return agenda.getTimeSource().getTime();
  }

  public void newNodeJoins() {
    logger.info("a new node (" + nodeIds + ") is joining");
    Node node = null;
    if (nodeIds == 1) {
      node = new Seeder(this, nodeIds++);
    } else {
      node = new Node(this, nodeIds++);
    }
    nodes.add(node);
  }

  public NetworkNode pickRandomNode() {
    return CollectionUtils.pickRandom(nodes);
  }

  public static void main(String[] args) {
    new SimulationNetwork();
  }

  public void connect(NetworkNode source, NetworkNode target) {
    if (!target.isFirewalled()) {
      Pair<NetworkSocket, NetworkSocket> sockets =
          Socket.createSocketPair(this, source, target);
      agenda.scheduleRandomized(new NewIncomingConnectionEvent(target, sockets.second), 250, 10);
      agenda.scheduleRandomized(new NewOutgoingConnectionEvent(source, sockets.first), 250, 10);
    }
  }

  public CounterRepository getRepository() {
    return agenda.getRepository();
  }

  private class NewIncomingConnectionEvent extends Event {
    private final NetworkNode node;
    private final NetworkSocket socket;

    public NewIncomingConnectionEvent(NetworkNode node, NetworkSocket socket) {
      this.node = node;
      this.socket = socket;
    }

    public void execute() throws Exception {
      node.newIncomingConnection(socket);
    }
  }

  private class NewOutgoingConnectionEvent extends Event {
    private final NetworkNode node;
    private final NetworkSocket socket;

    public NewOutgoingConnectionEvent(NetworkNode node, NetworkSocket socket) {
      this.node = node;
      this.socket = socket;
    }

    public void execute() throws Exception {
      node.newOutgoingConnection(socket);
    }
  }

  private class CleanupEvent extends Event {
    public void execute() throws Exception {
      Map<Tuple, Integer> tuples = new HashMap<Tuple, Integer>();
      int firewalled = 0;
      for (Iterator<NetworkNode> it = nodes.iterator(); it.hasNext();) {
        NetworkNode node = it.next();
        if (!node.isOnline()) {
          it.remove();
        } else {
          for (Tuple tuple : node.getAvailableTuples()) {
            Integer count = tuples.get(tuple);
            count = count != null ? count + 1 : 1;
            tuples.put(tuple, count);
          }
          if (node.isFirewalled()) {
            firewalled++;
          }
        }
      }
      getRepository().getCounter("nodes").set(nodes.size());
      getRepository().getCounter("nodes.firewalled").set(firewalled);
      getRepository().getCounter("tuples.diversity").set(tuples.size());

      int minCount = Integer.MAX_VALUE;
      int maxCount = Integer.MIN_VALUE;
      long average = 0;
      for (Integer count : tuples.values()) {
        average += count;
        minCount = Math.min(count, minCount);
        maxCount = Math.max(count, maxCount);
      }
      getRepository().getCounter("tuples.minoccurrence").set(minCount);
      getRepository().getCounter("tuples.maxoccurrence").set(maxCount);
      getRepository().getCounter("tuples.avgoccurrence").set(average / tuples.size());
    }

    public long getRescheduleInterval() {
      return 60 * 1000;
    }
  }

  private class NewNodeJoinsEvent extends Event {

    private final ExtendedRandom rnd = new ExtendedRandom();

    private static final int MAX_PEERS = 2000;

    public void execute() {
      newNodeJoins();
    }

    public boolean doRun() {
      return nodeIds <= MAX_PEERS;
    }

    public long getRescheduleInterval() {
      double expectation = 36000 / (MAX_PEERS + 2 - nodeIds); // seconds
      return (long) Math.max(1, rnd.nextExponential(1/expectation) * 1000.0); // milliseconds
    }
  }

  private class NewSingleNodeJoinsEvent extends Event {
    public void execute() {
      newNodeJoins();
    }
  }

  private class TimeLoggingEvent extends Event {
    public void execute() throws Exception {
      logger.info("simulation time is " + new Date(getTime()));
    }

    public long getRescheduleInterval() {
      return 60 * 1000;
    }
  }
}
