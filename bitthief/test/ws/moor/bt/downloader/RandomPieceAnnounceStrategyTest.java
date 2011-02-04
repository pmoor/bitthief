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

import org.easymock.classextension.EasyMock;
import ws.moor.bt.network.BitTorrentConnection;
import ws.moor.bt.storage.BitField;
import ws.moor.bt.torrent.MetaInfo;
import ws.moor.bt.util.ExtendedTestCase;

import java.net.InetSocketAddress;

/**
 * TODO(pmoor): Javadoc
 */
public class RandomPieceAnnounceStrategyTest extends ExtendedTestCase {

  private static final int PIECE_COUNT = 42;
  private static final int SET_PIECE_PERCENTAGE = 25;

  private InetSocketAddress address;
  private InetSocketAddress addressB;
  private MetaInfo metaInfo;
  private TorrentDownload torrentDownload;
  private BitTorrentConnection connection;
  private BitTorrentConnection connectionB;

  public void testGettingBitfield() {
    PieceAnnounceStrategy strategy =
        new RandomPieceAnnounceStrategy(torrentDownload, connection, SET_PIECE_PERCENTAGE);

    BitField bitField = strategy.getBitFieldToSend();
    assertEquals(PIECE_COUNT * SET_PIECE_PERCENTAGE / 100, bitField.getAvailablePieceCount());
    assertEquals(PIECE_COUNT, bitField.getPieceCount());
    assertEquals(bitField, strategy.getBitFieldToSend());
  }

  public void testAnnouncingPieces() {
    PieceAnnounceStrategy strategy =
        new RandomPieceAnnounceStrategy(torrentDownload, connection, SET_PIECE_PERCENTAGE);
    assertFalse(strategy.announcePiece(18));
    assertFalse(strategy.announcePiece(17));
    assertFalse(strategy.announcePiece(22));
  }

  public void testTwoDifferentConnections() {
    PieceAnnounceStrategy strategy =
        new RandomPieceAnnounceStrategy(torrentDownload, connection, SET_PIECE_PERCENTAGE);
    PieceAnnounceStrategy strategyB =
        new RandomPieceAnnounceStrategy(torrentDownload, connection, SET_PIECE_PERCENTAGE);

    assertEquals(strategy.getBitFieldToSend(), strategyB.getBitFieldToSend());
  }

  protected void tearDown() throws Exception {
    EasyMock.verify(connectionB);
    EasyMock.verify(connection);
    EasyMock.verify(torrentDownload);
    EasyMock.verify(metaInfo);
    super.tearDown();
  }

  protected void setUp() throws Exception {
    super.setUp();

    address = randomInetSocketAddress();
    addressB = randomInetSocketAddress();

    metaInfo = EasyMock.createMock(MetaInfo.class);
    EasyMock.expect(metaInfo.getPieceCount()).andReturn(PIECE_COUNT).anyTimes();
    EasyMock.replay(metaInfo);

    torrentDownload = EasyMock.createMock(TorrentDownload.class);
    EasyMock.expect(torrentDownload.getMetaInfo()).andReturn(metaInfo).anyTimes();
    EasyMock.replay(torrentDownload);

    connection = EasyMock.createMock(BitTorrentConnection.class);
    EasyMock.expect(connection.getRemoteAddress()).andReturn(address).anyTimes();
    EasyMock.replay(connection);

    connectionB = EasyMock.createMock(BitTorrentConnection.class);
    EasyMock.expect(connectionB.getRemoteAddress()).andReturn(addressB).anyTimes();
    EasyMock.replay(connectionB);
  }
}
