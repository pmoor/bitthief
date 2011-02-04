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
import ws.moor.bt.simulation.packets.AvailableTuplesPacket;
import ws.moor.bt.simulation.packets.Packet;
import ws.moor.bt.simulation.packets.PacketVisitor;
import ws.moor.bt.simulation.packets.ResetAvailableTuplesPacket;
import ws.moor.bt.simulation.packets.TupleAvailablePacket;
import ws.moor.bt.simulation.packets.TuplePacket;
import ws.moor.bt.simulation.packets.TupleRequestPacket;
import ws.moor.bt.util.LoggingUtil;
import ws.moor.bt.util.TimeSource;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO(pmoor): Javadoc
 */
public class Connection {

  private final Node owner;
  private final NetworkSocket socket;
  private final TimeSource timeSource;
  private final NextTupleDeterminer determiner;
  private final TupleAnnounceStrategy announceStrategy;

  private Tuple myPendingRequest;
  private Tuple remotePendingRequest;

  private int balance = 0;

  private Set<Tuple> remoteTuples = new HashSet<Tuple>();

  private long lastTupleReceived;

  private static final Logger logger = LoggingUtil.getLogger(Connection.class);

  public Connection(Node owner, NetworkSocket socket, TimeSource timeSource) {
    this.owner = owner;
    this.socket = socket;
    this.timeSource = timeSource;

    lastTupleReceived = timeSource.getTime();
    determiner = owner.getNextTupleDeterminer();
    announceStrategy = owner.getTupleAnnounceStrategy();

    logger.info("new connection from " + owner + " to " + getRemote());

    socket.addListener(new Listener());
    socket.send(new AvailableTuplesPacket(announceStrategy.getAvailableTuples(getRemote())));
  }

  public NetworkNode getRemote() {
    return socket.getRemote();
  }

  public void newTupleAvailable(Tuple tuple) {
    socket.send(new TupleAvailablePacket(tuple));
  }

  private void sendTupleIfAllowed() {
    if (remotePendingRequest != null) {
      if (balance <= 0 || (owner.isSeeder() && balance < owner.getBlockCount() / 3)) {
        logger.debug("sending tuple " + remotePendingRequest + " from " + owner + " to " + getRemote());
        balance++;
        socket.send(new TuplePacket(remotePendingRequest, owner.isSeeder()));
        remotePendingRequest = null;
        requestNewTupleIfInterested();
      }
    }
  }

  private void requestNewTupleIfInterested() {
    if (balance >= 0 && myPendingRequest == null) {
      Tuple nextTuple = determiner.getNextTuple(remoteTuples);
      if (nextTuple != null) {
        logger.debug("requesting tuple " + nextTuple + " from " + getRemote() + " for " + owner);
        socket.send(new TupleRequestPacket(nextTuple));
        myPendingRequest = nextTuple;
      }
    }
  }

  public void closeConnection() {
    logger.info("closing connection from " + owner + " to " + getRemote());
    socket.close();
  }

  public void changingToSeed() {
    socket.send(new ResetAvailableTuplesPacket());
    socket.send(new AvailableTuplesPacket(announceStrategy.getAvailableTuples(getRemote())));
  }

  public long timeSinceLastReceivedTuple() {
    return timeSource.getTime() - lastTupleReceived;
  }

  private class Listener implements NetworkSocket.Listener {

    final Visitor visitor = new Visitor();

    public void receive(Packet message) {
      message.accept(visitor);
    }

    public void close() {
      owner.closedConnection(Connection.this);
    }
  }

  private class Visitor implements PacketVisitor {
    public void visitAvailableTuplesPacket(AvailableTuplesPacket availableTuples) {
      logger.debug("got available tuples message from " + getRemote());
      remoteTuples.addAll(availableTuples.getTuples());
      requestNewTupleIfInterested();
    }

    public void visitTuplePacket(TuplePacket tuplePacket) {
      lastTupleReceived = timeSource.getTime();
      if (!tuplePacket.isFree()) {
        balance--;
      }
      owner.addTuple(tuplePacket.getTuple());
      myPendingRequest = null;
      requestNewTupleIfInterested();
      sendTupleIfAllowed();
    }

    public void visitTupleRequestPacket(TupleRequestPacket requestPacket) {
      remotePendingRequest = requestPacket.getTuple();
      sendTupleIfAllowed();
      requestNewTupleIfInterested();
    }

    public void visitTupleAvailablePacket(TupleAvailablePacket tupleAvailablePacket) {
      remoteTuples.add(tupleAvailablePacket.getTuple());
      owner.newTupleSeen(tupleAvailablePacket.getTuple());
      sendTupleIfAllowed();
      requestNewTupleIfInterested();
    }

    public void visitResetAvailableTuplesPacket(ResetAvailableTuplesPacket packet) {
      remoteTuples.clear();
    }
  }
}
