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

import ws.moor.bt.util.TimeSource;

import java.util.Random;

/**
 * TODO(pmoor): Javadoc
 */
public class RateLimiter {

  // space units / time units
  private final int rate;
  private final int delay;
  private final TimeSource timeSource;

  private long reservedUpTo;

  private static final Random rnd = new Random();

  /**
   *
   * @param rate KB/s
   * @param delay miliseconds
   * @param timeSource
   */
  public RateLimiter(int rate, int delay, TimeSource timeSource) {
    this.rate = rate;
    this.delay = delay;
    this.timeSource = timeSource;
    reservedUpTo = 0;
  }


  public long calculatePacketArrivalTime(int dataSize) {
    reservedUpTo = Math.max(timeSource.getTime(), reservedUpTo);
    reservedUpTo += dataSize / rate;
    return reservedUpTo + delay;
  }

  public int getRate() {
    return rate;
  }

  public int getDelay() {
    return delay;
  }

  public String toString() {
    return rate + "/" + delay;
  }
}
