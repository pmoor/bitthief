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

package ws.moor.bt.network;

/**
 * TODO(pmoor): Javadoc
 */
public enum ConnectionCloseReason {
  BITFIELD_DOWNCAST_ERROR("cannot downcast bitfield"),
  EXCEPTION_WHILE_SENDING("general exception during send"),
  NO_TORRENT_RUNNING("no torrent running for infohash"),
  NOT_ALLOWED_TO_LEECH("not allowed to leech from seeds"),
  SOCKET_CLOSED("underlying socket is closed"),
  INACTIVITY("inactivity"),
  CONNECT_TO_MYSELF("connection to myself"),
  ONLY_REFUSED_REQUESTS("only refused requests in the uploading queue"),
  TORRENT_STOPED("torrent download has been stoped");

  private final String reason;

  ConnectionCloseReason(String reason) {
    this.reason = reason;
  }

  public String toString() {
    return reason;
  }
}
