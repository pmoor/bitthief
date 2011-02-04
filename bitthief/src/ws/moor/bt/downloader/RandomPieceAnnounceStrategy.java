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

import ws.moor.bt.network.BitTorrentConnection;
import ws.moor.bt.storage.BitField;

import java.util.Random;

/**
 * TODO(pmoor): Javadoc
 */
public class RandomPieceAnnounceStrategy implements PieceAnnounceStrategy {

  private final BitField bitField;

  public RandomPieceAnnounceStrategy(TorrentDownload download,
                                     BitTorrentConnection connection,
                                     int percentHaving) {
    int pieceCount = download.getMetaInfo().getPieceCount();
    bitField = new BitField(pieceCount);
    bitField.setRandomPieces(pieceCount * percentHaving / 100, getRandomness(connection));
  }

  private Random getRandomness(BitTorrentConnection connection) {
    String address = connection.getRemoteAddress().getAddress().getHostAddress();
    return new Random((long) address.hashCode());
  }

  public BitField getBitFieldToSend() {
    return bitField;
  }

  public boolean announcePiece(int pieceIndex) {
    return false;
  }
}
