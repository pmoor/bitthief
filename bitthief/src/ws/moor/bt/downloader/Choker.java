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

package ws.moor.bt.downloader;

import org.apache.log4j.Logger;
import ws.moor.bt.network.BitTorrentConnection;
import ws.moor.bt.util.LoggingUtil;
import ws.moor.bt.util.SystemTimeSource;
import ws.moor.bt.util.TimeSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO(pmoor): Javadoc
 */
public class Choker {
  private final TorrentDownload torrentDownload;
  private final int uploadSlots;
  private final int randomUnchokes;

  private long lastPeriod = 0;

  private TimeSource timeSource = SystemTimeSource.INSTANCE;

  private static final long CHOKE_INTERVAL = 30 * 1000;
  private static final Logger logger = LoggingUtil.getLogger(Choker.class);

  public Choker(TorrentDownload torrentDownload, int uploadSlots) {
    this.torrentDownload = torrentDownload;
    this.uploadSlots = uploadSlots;
    randomUnchokes = uploadSlots / 6;
  }

  public void maintenance() {
    if (timeSource.getTime() > lastPeriod + CHOKE_INTERVAL) {
      try {
        closeConnectionsToPeersWithOnlyRefusedRequestsPending();
        chokeOrUnchokePeers();
      } catch (Exception e) {
        logger.error("exception while choking/unchoking", e);
      }
      lastPeriod = timeSource.getTime();
    }
  }

  private void closeConnectionsToPeersWithOnlyRefusedRequestsPending() {
    logger.info("closing connections with refused requests");
    List<BitTorrentConnection> connections =
        new ArrayList<BitTorrentConnection>(torrentDownload.getAllValidConnections());
    for (BitTorrentConnection connection : connections) {
      connection.closeIfOnlyRefusedRequestsPending();
    }
  }

  private void chokeOrUnchokePeers() {
    logger.info("choking/unchoking peers");
    List<BitTorrentConnection> connections =
        new ArrayList<BitTorrentConnection>(torrentDownload.getAllValidConnections());
    Collections.sort(connections, new Comparator<BitTorrentConnection>() {
      public int compare(BitTorrentConnection o1, BitTorrentConnection o2) {
        long v1 = o1.getDownloadScore();
        long v2 = o2.getDownloadScore();
        if (v1 < v2) {
          return +1;
        } else if (v1 > v2) {
          return -1;
        }
        if (o1.isRemoteInterested() && !o2.isRemoteInterested()) {
          return -1;
        } else if (!o1.isRemoteInterested() && o2.isRemoteInterested()) {
          return +1;
        }
        return 0;
      }
    });
    Set<BitTorrentConnection> unchoked =
        new HashSet<BitTorrentConnection>(
            connections.subList(0, Math.min(uploadSlots - randomUnchokes, connections.size())));
    addRandomUnchokes(unchoked, connections);
    for (BitTorrentConnection connection : connections) {
      if (unchoked.contains(connection)) {
        logger.debug("unchoking peer with score " + connection.getDownloadScore());
        connection.unchokeRemote();
      } else {
        logger.debug("choking peer with score " + connection.getDownloadScore());
        connection.chokeRemote();
      }
    }
  }

  private void addRandomUnchokes(Set<BitTorrentConnection> unchoked, List<BitTorrentConnection> connections) {
    Collections.shuffle(connections);
    int added = 0;
    for (BitTorrentConnection connection : connections) {
      if (added == randomUnchokes) {
        break;
      } else if (!unchoked.contains(connection)) {
        unchoked.add(connection);
        added++;
      }
    }
  }
}
