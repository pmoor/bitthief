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
import ws.moor.bt.storage.BitField;
import ws.moor.bt.storage.PieceManager;
import ws.moor.bt.torrent.MetaInfo;
import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class RealPieceAnnounceStrategyTest extends ExtendedTestCase {

  private static final int PIECE_COUNT = 42;

  private MetaInfo metaInfo;
  private TorrentDownload torrentDownload;
  private PieceManager pieceManager;

  private BitField bitField;

  public void testGettingBitfield() {
    PieceAnnounceStrategy strategy =
        new RealPieceAnnounceStrategy(torrentDownload);

    BitField bitField = strategy.getBitFieldToSend();
    assertEquals(this.bitField, bitField);
  }

  public void testAnnouncingPieces() {
    PieceAnnounceStrategy strategy = new RealPieceAnnounceStrategy(torrentDownload);
    assertTrue(strategy.announcePiece(18));
    assertTrue(strategy.announcePiece(17));
    assertTrue(strategy.announcePiece(22));
  }

  protected void tearDown() throws Exception {
    EasyMock.verify(torrentDownload);
    EasyMock.verify(pieceManager);
    EasyMock.verify(metaInfo);
    super.tearDown();
  }

  protected void setUp() throws Exception {
    super.setUp();

    bitField = new BitField(PIECE_COUNT);
    bitField.setRandomPieces(PIECE_COUNT / 2, rnd);

    metaInfo = EasyMock.createMock(MetaInfo.class);
    EasyMock.expect(metaInfo.getPieceCount()).andReturn(PIECE_COUNT).anyTimes();
    EasyMock.replay(metaInfo);

    pieceManager = EasyMock.createMock(PieceManager.class);
    EasyMock.expect(pieceManager.getValidPieces()).andReturn(bitField).anyTimes();
    EasyMock.replay(pieceManager);

    torrentDownload = EasyMock.createMock(TorrentDownload.class);
    EasyMock.expect(torrentDownload.getMetaInfo()).andReturn(metaInfo).anyTimes();
    EasyMock.expect(torrentDownload.getPieceManager()).andReturn(pieceManager).anyTimes();
    EasyMock.replay(torrentDownload);
  }
}
