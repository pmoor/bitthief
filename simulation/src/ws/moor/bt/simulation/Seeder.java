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

public class Seeder extends Node {
  private static final int SEDER_RATE = 256;
  private static final int SEDER_DELAY = 40;

  public Seeder(SimulationNetwork simulationNetwork, int peerId) {
    super(simulationNetwork, peerId);

    mode = NodeMode.SEEDER;

    simulationNetwork.getAgenda().scheduleDelta(new CheckShouldQuitEvent(), 0);
  }

  public double getCompletionRate() {
    return 1.0;
  }

  public boolean isFirewalled() {
    return false;
  }

  protected RateLimiter constructOutgoingRateLimiter() {
    return new RateLimiter(SEDER_RATE, SEDER_DELAY, network.getAgenda().getTimeSource());
  }

  protected boolean acceptIncomingConnection() {
    return connections.size() < 50;
  }

  protected boolean removeAnOldConnection() {
    return connections.size() > 30;
  }

  private class CheckShouldQuitEvent extends Event {
    public void execute() throws Exception {
      if (packetsSent() > 4 * getBlockCount()) {
        goOffline();
      }
    }

    private long packetsSent() {
      return network.getRepository().getCounter("packets.sent", Long.toString(getId())).get();
    }

    public boolean doRun() {
      return isOnline();
    }

    public long getRescheduleInterval() {
      return 900 * 1000;
    }
  }
}
