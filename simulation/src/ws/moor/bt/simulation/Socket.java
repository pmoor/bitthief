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

import ws.moor.bt.simulation.packets.Packet;
import ws.moor.bt.stats.Counter;
import ws.moor.bt.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO(pmoor): Javadoc
 */
public class Socket implements NetworkSocket {

  private final SimulationNetwork network;
  private final ArrayList<Listener> listeners = new ArrayList<Listener>();
  private final NetworkNode localPeer;
  private final NetworkNode remotePeer;

  private Map<Class, Integer> packetsSent =
      new HashMap<Class, Integer>();
  private boolean open = true;

  private Socket otherSocket;

  private boolean connectionLevelStats = false;

  private final Counter bytesSentCounter;
  private final Counter bytesReceivedCounter;
  private final Counter packetsSentCounter;
  private final Counter packetsReceivedCounter;

  public Socket(SimulationNetwork network, NetworkNode localPeer, NetworkNode remotePeer) {
    this.network = network;
    this.localPeer = localPeer;
    this.remotePeer = remotePeer;
    bytesSentCounter = network.getRepository().getCounter("bytes.sent", Long.toString(localPeer.getId()));
    bytesReceivedCounter = network.getRepository().getCounter("bytes.received", Long.toString(remotePeer.getId()));
    packetsSentCounter = network.getRepository().getCounter("packets.sent", Long.toString(localPeer.getId()));
    packetsReceivedCounter = network.getRepository().getCounter("packets.received", Long.toString(remotePeer.getId()));
  }

  public void send(Packet message) {
    if (!open) {
      return;
    }

    long arrivalTime = calculateArrivalTime(message.getSizeOnWire());
    network.getAgenda().schedule(new SendEvent(message), arrivalTime);
    increasePacketCount(message);
  }

  private long calculateArrivalTime(int messageSize) {
    long localTime =
        localPeer.getOutgoingRateLimiter().calculatePacketArrivalTime(messageSize);
    long remoteTime =
        remotePeer.getIncomingRateLimiter().calculatePacketArrivalTime(messageSize);
    return Math.max(localTime, remoteTime);
  }

  private void increasePacketCount(Packet packet) {
    Class key = packet.getClass();
    Integer value = packetsSent.get(key);
    if (value == null) {
      value = 0;
    }
    packetsSent.put(key, ++value);

    if (connectionLevelStats) {
      network.getRepository().getCounter("bytes.sent", localPeer.getId() + ":" + remotePeer.getId()).increase(packet.getSizeOnWire());
      network.getRepository().getCounter("bytes.received", remotePeer.getId() + ":" + localPeer.getId()).increase(packet.getSizeOnWire());
      network.getRepository().getCounter("packets.sent", localPeer.getId() + ":" + remotePeer.getId()).increase(1);
      network.getRepository().getCounter("packets.received", remotePeer.getId() + ":" + localPeer.getId()).increase(1);
    }

    bytesSentCounter.increase(packet.getSizeOnWire());
    bytesReceivedCounter.increase(packet.getSizeOnWire());
    packetsSentCounter.increase(1);
    packetsReceivedCounter.increase(1);
  }

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public NetworkNode getRemote() {
    return remotePeer;
  }

  public void close() {
    if (!open) {
      return;
    }
    long arrivalTime = calculateArrivalTime(0);
    network.getAgenda().schedule(new CloseEvent(), arrivalTime);
    open = false;
  }

  private void setOtherSocket(Socket otherSocket) {
    this.otherSocket = otherSocket;
  }

  private class CloseEvent extends Event {
    public void execute() {
      otherSocket.open = false;
      for (Listener listener : otherSocket.listeners) {
        listener.close();
      }
    }
  }

  private class SendEvent extends Event {
    private final Packet message;

    public SendEvent(Packet message) {
      this.message = message;
    }

    public void execute() {
      for (Listener listener : otherSocket.listeners) {
        listener.receive(message);
      }
    }
  }


  public static Pair<NetworkSocket, NetworkSocket> createSocketPair(SimulationNetwork network,
                                                                    NetworkNode source,
                                                                    NetworkNode target) {
    Socket sourceSocket = new Socket(network, source, target);
    Socket destinationSocket = new Socket(network, target, source);
    sourceSocket.setOtherSocket(destinationSocket);
    destinationSocket.setOtherSocket(sourceSocket);
    return new Pair<NetworkSocket, NetworkSocket>(sourceSocket, destinationSocket);
  }
}
