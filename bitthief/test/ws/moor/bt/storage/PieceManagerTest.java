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

package ws.moor.bt.storage;

import org.easymock.classextension.EasyMock;
import ws.moor.bt.Environment;
import ws.moor.bt.downloader.Block;
import ws.moor.bt.stats.FakeRepository;
import ws.moor.bt.torrent.Hash;
import ws.moor.bt.torrent.HashTest;
import ws.moor.bt.torrent.MetaInfo;
import ws.moor.bt.torrent.MultiFileMetaInfo;
import ws.moor.bt.util.ArrayUtil;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.ExtendedTestCase;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * TODO(pmoor): Javadoc
 */
public class PieceManagerTest extends ExtendedTestCase {

  private MetaInfo metaFile;

  private static final int PIECE_LENGTH = 128;
  private static final int LAST_PIECE_LENGTH = 64;
  private byte[][] pieces;
  private Hash[] hashes;
  private MetaInfo.FileInfo[] infos;
  private Storage storage;
  private Environment environment;

  protected void setUp() throws Exception {
    super.setUp();

    pieces = new byte[33][];
    for (int i = 0; i < pieces.length - 1; i++) {
      pieces[i] = ByteUtil.randomByteArray(PIECE_LENGTH);
    }
    pieces[pieces.length - 1] = ByteUtil.randomByteArray(LAST_PIECE_LENGTH);

    hashes = new Hash[33];
    for (int i = 0; i < hashes.length; i++) {
      hashes[i] = Hash.forByteArray(pieces[i]);
    }

    infos = new MetaInfo.FileInfo[3];
    infos[0] = new MetaInfo.FileInfo(new String[] {"a"}, 16 * 128);
    infos[1] = new MetaInfo.FileInfo(new String[] {"b"},  8 * 128);
    infos[2] = new MetaInfo.FileInfo(new String[] {"c"}, 17 *  64);

    metaFile = new MultiFileMetaInfo(
        HashTest.randomHash(),
        new URL("http://moor.ws/"),
        null,
        new Date(),
        PIECE_LENGTH,
        "basedir", hashes, infos);

    StorageBuilder storageBuilder = new StorageBuilder();
    storageBuilder.setFileRepository(new VirtualFileRepository());
    storage = storageBuilder.buildStorage(metaFile, new File("/"));

    environment = EasyMock.createMock(Environment.class);
    EasyMock.expect(environment.getExecutor()).andReturn(new Executor() {
      public void execute(Runnable command) {
        command.run();
      }
    }).anyTimes();
    EasyMock.replay(environment);
  }

  public void testPristineStorage() {
    PieceManager pieceManager = createDefaultPieceManager();
    assertEquals(new BitField(33), pieceManager.getValidPieces());
  }

  public void testStorageWithSomeData() {
    assertEquals(pieces[7].length, storage.writePiece(7, pieces[7]));
    assertEquals(pieces[13].length, storage.writePiece(13, pieces[13]));
    PieceManager pieceManager = createDefaultPieceManager();
    assertEquals(new BitField(33, 7, 13), pieceManager.getValidPieces());
  }

  public void testPieceAvailable() {
    assertEquals(pieces[7].length, storage.writePiece(7, pieces[7]));
    assertEquals(pieces[13].length, storage.writePiece(13, pieces[13]));
    PieceManager pieceManager = createDefaultPieceManager();

    assertTrue(pieceManager.isPieceAvailable(7));
    assertTrue(pieceManager.isPieceAvailable(13));
    assertFalse(pieceManager.isPieceAvailable(2));
    assertFalse(pieceManager.isPieceAvailable(0));
    assertFalse(pieceManager.isPieceAvailable(32));

    try {
      assertFalse(pieceManager.isPieceAvailable(33));
      fail("should fail");
    } catch (IndexOutOfBoundsException e) {
      // expected
    }
  }

  public void testNonExistingBlock() {
    PieceManager pieceManager = createDefaultPieceManager();

    assertGetBlockThrowsIllegalArgumentException(pieceManager, new Block(0, 0, 5));
  }

  public void testIllegalArguments() {
    assertEquals(pieces[7].length, storage.writePiece(7, pieces[7]));
    PieceManager pieceManager = createDefaultPieceManager();

    assertGetBlockThrowsIllegalArgumentException(pieceManager, new Block(7, -1, 5));
    assertGetBlockThrowsIllegalArgumentException(pieceManager, new Block(7, PIECE_LENGTH, 5));
    assertGetBlockThrowsIllegalArgumentException(pieceManager, new Block(7, 5, -1));
    assertGetBlockThrowsIllegalArgumentException(pieceManager, new Block(7, 5, PIECE_LENGTH));
    assertGetBlockThrowsIllegalArgumentException(pieceManager, new Block(7, -5, PIECE_LENGTH));
  }

  public void testValidBlockFetch() {
    assertEquals(pieces[7].length, storage.writePiece(7, pieces[7]));
    assertEquals(pieces[3].length, storage.writePiece(3, pieces[3]));
    PieceManager pieceManager = createDefaultPieceManager();

    assertEquals(new DataBlock(7, 15, ArrayUtil.subArray(pieces[7], 15, 35)),
        pieceManager.getBlock(new Block(7, 15, 35)));
    assertEquals(new DataBlock(3, 0, pieces[3]),
        pieceManager.getBlock(new Block(3, 0, PIECE_LENGTH)));
    assertEquals(new DataBlock(3, PIECE_LENGTH - 1, ArrayUtil.subArray(pieces[3], PIECE_LENGTH - 1, 1)),
        pieceManager.getBlock(new Block(3, PIECE_LENGTH - 1, 1)));
  }

  public void testFetchWithoutVerify() {
    PieceManager pieceManager = createDefaultPieceManager();
    try {
      pieceManager.getBlock(new Block(3, 0, 5));
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testAddSomeCompletePieces() {
    PieceManager pieceManager = createDefaultPieceManager();

    byte[] currentStorageContent = new byte[PIECE_LENGTH];
    storage.readPiece(0, currentStorageContent);
    assertArrayEquals(new byte[PIECE_LENGTH], currentStorageContent);

    byte[] dataToWrite = new byte[32];
    System.arraycopy(pieces[0], 70, dataToWrite, 0, 32);
    pieceManager.setBlock(new DataBlock(0, 70, dataToWrite));

    System.arraycopy(pieces[0], 30, dataToWrite, 0, 32);
    pieceManager.setBlock(new DataBlock(0, 30, dataToWrite));

    System.arraycopy(pieces[0], 60, dataToWrite, 0, 32);
    pieceManager.setBlock(new DataBlock(0, 60, dataToWrite));

    System.arraycopy(pieces[0], 96, dataToWrite, 0, 32);
    pieceManager.setBlock(new DataBlock(0, 96, dataToWrite));

    System.arraycopy(pieces[0], 0, dataToWrite, 0, 32);
    pieceManager.setBlock(new DataBlock(0, 0, dataToWrite));

    byte[] newStorageContent = new byte[PIECE_LENGTH];
    storage.readPiece(0, newStorageContent);
    assertArrayEquals(pieces[0], newStorageContent);
  }

  public void testAddSomeIncompletePieces() {
    PieceManager pieceManager = createDefaultPieceManager();

    byte[] currentStorageContent = new byte[PIECE_LENGTH];
    storage.readPiece(0, currentStorageContent);
    assertArrayEquals(new byte[PIECE_LENGTH], currentStorageContent);

    byte[] dataToWrite = new byte[32];
    System.arraycopy(pieces[0], 30, dataToWrite, 0, 32);
    pieceManager.setBlock(new DataBlock(0, 30, dataToWrite));

    System.arraycopy(pieces[0], 60, dataToWrite, 0, 32);
    pieceManager.setBlock(new DataBlock(0, 60, dataToWrite));

    byte[] newStorageContent = new byte[PIECE_LENGTH];
    storage.readPiece(0, newStorageContent);
    assertArrayEquals(new byte[PIECE_LENGTH], newStorageContent);
  }

  public void testAddWrongPieces() {
    PieceManager pieceManager = createDefaultPieceManager();

    byte[] currentStorageContent = new byte[PIECE_LENGTH];
    storage.readPiece(0, currentStorageContent);
    assertArrayEquals(new byte[PIECE_LENGTH], currentStorageContent);

    pieceManager.setBlock(new DataBlock(0, 0, new byte[PIECE_LENGTH]));

    byte[] newStorageContent = new byte[PIECE_LENGTH];
    storage.readPiece(0, newStorageContent);
    assertArrayEquals(new byte[PIECE_LENGTH], newStorageContent);
  }

  public void testGetMissingBlocksNone() {
    assertEquals(pieces[7].length, storage.writePiece(7, pieces[7]));
    PieceManager pieceManager = createDefaultPieceManager();

    List<Block> blocks = pieceManager.getMissingBlocks(7, 32);
    assertTrue(blocks.isEmpty());
  }

  public void testUpdatesBitFieldCorrectly() {
    PieceManager pieceManager = createDefaultPieceManager();
    assertFalse(pieceManager.getValidPieces().hasPiece(7));

    pieceManager.setBlock(new DataBlock(7, 0, pieces[7]));
    assertTrue(pieceManager.getValidPieces().hasPiece(7));
  }

  public void testListeners() {
    PieceListener listener = EasyMock.createMock(PieceListener.class);
    listener.gotPiece(1);
    listener.gotPiece(5);
    EasyMock.replay(listener);

    PieceManager pieceManager = createDefaultPieceManager();
    pieceManager.setBlock(new DataBlock(0, 0, pieces[0]));
    pieceManager.addPieceListener(listener);
    pieceManager.setBlock(new DataBlock(1, 0, pieces[1]));
    pieceManager.setBlock(new DataBlock(5, 0, pieces[5]));
    pieceManager.removePieceListener(listener);
    pieceManager.setBlock(new DataBlock(6, 0, pieces[6]));

    EasyMock.verify(listener);
  }

  public void testIsBitFieldOfInterest() {
    PieceManager pieceManager = createDefaultPieceManager();
    assertFalse(pieceManager.isBitFieldOfInterest(null));

    pieceManager.setBlock(new DataBlock(7, 0, pieces[7]));
    BitField testing = pieceManager.getValidPieces();
    assertFalse(pieceManager.isBitFieldOfInterest(testing));

    testing.gotPiece(2);
    assertTrue(pieceManager.isBitFieldOfInterest(testing));
  }

  private void assertGetBlockThrowsIllegalArgumentException(
      PieceManager pieceManager, Block block) {
    try {
      pieceManager.getBlock(block);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  private PieceManager createDefaultPieceManager() {
    return new PieceManager(metaFile, storage, environment, new FakeRepository());
  }
}
