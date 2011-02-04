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

package ws.moor.bt.downloader.peermanager;

import ws.moor.bt.util.TimeSource;

import java.net.InetSocketAddress;

/**
 * TODO(pmoor): Javadoc
 */
class PeerRecord {

  private final InetSocketAddress address;
  private final long created;
  private long lastSeenFromTracker;
  private long lastScheduledForConnection;

  private final TimeSource timeSource;

  private static final int ONE_DAY = 86400 * 1000;

  public PeerRecord(InetSocketAddress address, TimeSource timeSource) {
    assertNotNull(address);
    this.address = address;
    assertNotNull(timeSource);
    this.timeSource = timeSource;

    lastSeenFromTracker = created = timeSource.getTime();
    lastScheduledForConnection = created - ONE_DAY;
  }

  private void assertNotNull(Object object) {
    if (object == null) {
      throw new NullPointerException();
    }
  }

  public InetSocketAddress getAddress() {
    return address;
  }

  public void seenByTracker() {
    lastSeenFromTracker = timeSource.getTime();
  }

  public void scheduleForConnectionInitiation() {
    lastScheduledForConnection = timeSource.getTime();
  }

  public long getLastScheduledForConnectionTime() {
    return lastScheduledForConnection;
  }

  public int hashCode() {
    return address.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != getClass()) {
      return false;
    }
    return address.equals(((PeerRecord) obj).address);
  }

  public String toString() {
    return address.getAddress().getHostAddress() + ":" + address.getPort();
  }
}
