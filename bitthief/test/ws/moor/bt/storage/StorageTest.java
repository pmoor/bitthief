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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import ws.moor.bt.torrent.MetaInfo;
import ws.moor.bt.util.ExtendedTestCase;

import java.io.File;

/**
 * TODO(pmoor): Javadoc
 */
public class StorageTest extends ExtendedTestCase {

  private int defaultPieceSize = 100;
  private byte[] dataToWrite = new byte[100];

  private File parent = new File("/parent");

  private File fileA = new File(parent, "A");
  private File fileB = new File(parent, "B");
  private File fileC = new File(parent, "C");
  private File fileD = new File(parent, "D");
  private File fileG = new File(parent, "G/G/G/G");

  private MetaInfo.FileInfo fileInfoA = new MetaInfo.FileInfo(new String[] {"A"}, 350);
  private MetaInfo.FileInfo fileInfoB = new MetaInfo.FileInfo(new String[] {"B"}, 170);
  private MetaInfo.FileInfo fileInfoC = new MetaInfo.FileInfo(new String[] {"C"}, 32);
  private MetaInfo.FileInfo fileInfoD = new MetaInfo.FileInfo(new String[] {"D"}, 32);
  private MetaInfo.FileInfo fileInfoE = new MetaInfo.FileInfo(new String[] {"E/E"}, 0);
  private MetaInfo.FileInfo fileInfoF = new MetaInfo.FileInfo(new String[] {"F/F"}, 0);
  private MetaInfo.FileInfo fileInfoG = new MetaInfo.FileInfo(new String[] {"G/G/G/G"}, 32);

  protected void setUp() throws Exception {
    for (int i = 0; i < 100; i++) {
      dataToWrite[i] = (byte) (i * i);
    }
  }

  public void testWriteOneFileCase() {
    FileRepository repository = createMock(FileRepository.class);
    repository.write(eq(dataToWrite), eq(0L), eq(fileA), eq(0L), eq(100L));
    repository.write(eq(dataToWrite), eq(0L), eq(fileA), eq(100L), eq(100L));
    repository.write(eq(dataToWrite), eq(0L), eq(fileA), eq(200L), eq(100L));
    repository.write(eq(dataToWrite), eq(0L), eq(fileA), eq(300L), eq(50L));
    replay(repository);

    MetaInfo.FileInfo fileInfo[] = new MetaInfo.FileInfo[] {fileInfoA};
    Storage storage = new Storage(repository, parent, fileInfo, defaultPieceSize);

    assertEquals(100, storage.writePiece(0, dataToWrite));
    assertEquals(100, storage.writePiece(1, dataToWrite));
    assertEquals(100, storage.writePiece(2, dataToWrite));
    assertEquals( 50, storage.writePiece(3, dataToWrite));


    verify(repository);
  }

  public void testWriteTwoFileCase() {
    FileRepository repository = createMock(FileRepository.class);
    // 1st piece
    repository.write(eq(dataToWrite), eq(0L), eq(fileA), eq(0L), eq(100L));
    // 2nd piece
    repository.write(eq(dataToWrite), eq(0L), eq(fileA), eq(100L), eq(100L));
    // 3rd piece
    repository.write(eq(dataToWrite), eq(0L), eq(fileA), eq(200L), eq(100L));
    // 4th piece
    repository.write(eq(dataToWrite), eq(0L), eq(fileA), eq(300L), eq(50L));
    repository.write(eq(dataToWrite), eq(50L), eq(fileB), eq(0L), eq(50L));
    // 5th piece
    repository.write(eq(dataToWrite), eq(0L), eq(fileB), eq(50L), eq(100L));
    // 6th piece
    repository.write(eq(dataToWrite), eq(0L), eq(fileB), eq(150L), eq(20L));
    replay(repository);

    MetaInfo.FileInfo fileInfo[] = new MetaInfo.FileInfo[] {fileInfoA, fileInfoB};
    Storage storage = new Storage(
        repository, parent, fileInfo, defaultPieceSize);

    assertEquals(100, storage.writePiece(0, dataToWrite));
    assertEquals(100, storage.writePiece(1, dataToWrite));
    assertEquals(100, storage.writePiece(2, dataToWrite));
    assertEquals(100, storage.writePiece(3, dataToWrite));
    assertEquals(100, storage.writePiece(4, dataToWrite));
    assertEquals( 20, storage.writePiece(5, dataToWrite));

    verify(repository);
  }

  public void testWriteAWholeLotOfFiles() {
    FileRepository repository = createMock(FileRepository.class);
    // 1st piece
    repository.write(eq(dataToWrite), eq(0L), eq(fileB), eq(0L), eq(100L));
    // 2nd piece
    repository.write(eq(dataToWrite), eq(0L), eq(fileB), eq(100L), eq(70L));
    repository.write(eq(dataToWrite), eq(70L), eq(fileC), eq(0L), eq(30L));
    // 3rd piece
    repository.write(eq(dataToWrite), eq(0L), eq(fileC), eq(30L), eq(2L));
    repository.write(eq(dataToWrite), eq(2L), eq(fileD), eq(0L), eq(32L));
    repository.write(eq(dataToWrite), eq(34L), eq(fileG), eq(0L), eq(32L));
    replay(repository);

    MetaInfo.FileInfo fileInfo[] =
        new MetaInfo.FileInfo[] {fileInfoB, fileInfoC, fileInfoD, fileInfoE, fileInfoF, fileInfoG};
    Storage storage = new Storage(repository, parent, fileInfo, defaultPieceSize);

    assertEquals(100, storage.writePiece(0, dataToWrite));
    assertEquals(100, storage.writePiece(1, dataToWrite));
    assertEquals( 66, storage.writePiece(2, dataToWrite));

    verify(repository);
  }

  public void testWritePastEnd() {
    FileRepository repository = createMock(FileRepository.class);
    repository.write(eq(dataToWrite), eq(0L), eq(fileA), eq(300L), eq(50L));
    replay(repository);

    MetaInfo.FileInfo fileInfo[] = new MetaInfo.FileInfo[] {fileInfoA};
    Storage storage = new Storage(repository, parent, fileInfo, defaultPieceSize);

    assertEquals(50, storage.writePiece(3, dataToWrite));
    try {
      storage.writePiece(4, dataToWrite);
      fail("should not be allowed");
    } catch (IndexOutOfBoundsException ioobe) {
      // expected
    }

    verify(repository);
  }

  public void testWriteBeforeZero() {
    FileRepository repository = createMock(FileRepository.class);
    replay(repository);

    MetaInfo.FileInfo fileInfo[] = new MetaInfo.FileInfo[] {fileInfoA};
    Storage storage = new Storage(repository, parent, fileInfo, defaultPieceSize);

    try {
      storage.writePiece(-1, dataToWrite);
      fail("should not be allowed");
    } catch (IndexOutOfBoundsException ioobe) {
      // expected
    }

    verify(repository);
  }

  public void testWriteShortPackage() {
    FileRepository repository = createMock(FileRepository.class);
    replay(repository);

    MetaInfo.FileInfo fileInfo[] = new MetaInfo.FileInfo[] {fileInfoA};
    Storage storage = new Storage(repository, parent, fileInfo, defaultPieceSize);

    try {
      storage.writePiece(0, new byte[50]);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }

    verify(repository);
  }

  public void testWriteNull() {
    FileRepository repository = createMock(FileRepository.class);
    replay(repository);

    MetaInfo.FileInfo fileInfo[] = new MetaInfo.FileInfo[] {fileInfoA};
    Storage storage = new Storage(repository, parent, fileInfo, defaultPieceSize);

    try {
      storage.writePiece(0, null);
      fail("should fail");
    } catch (NullPointerException npe) {
      // expected
    }

    verify(repository);
  }

  public void testReadOneFileCaes() {
    byte[] buffer = new byte[defaultPieceSize];

    FileRepository repository = createMock(FileRepository.class);
    repository.read(eq(fileG), eq(0L), eq(buffer), eq(0L), eq(32L));
    replay(repository);

    MetaInfo.FileInfo fileInfo[] = new MetaInfo.FileInfo[] {fileInfoG};
    Storage storage = new Storage(repository, parent, fileInfo, defaultPieceSize);

    assertEquals(32, storage.readPiece(0, buffer));

    verify(repository);
  }

  public void testReadShort() {
    byte[] bufferA = new byte[32];
    byte[] bufferB = new byte[5];

    FileRepository repository = createMock(FileRepository.class);
    repository.read(eq(fileG), eq(0L), eq(bufferA), eq(0L), eq(32L));
    replay(repository);

    MetaInfo.FileInfo fileInfo[] = new MetaInfo.FileInfo[] {fileInfoG};
    Storage storage = new Storage(repository, parent, fileInfo, defaultPieceSize);

    assertEquals(32, storage.readPiece(0, bufferA));
    try {
      storage.readPiece(0, bufferB);
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }

    verify(repository);
  }

  public void testPieceCount() {
    Storage storage = new Storage(null, null, new MetaInfo.FileInfo[] {fileInfoA}, 50);
    assertEquals(7, storage.getPieceCount());

    storage = new Storage(null, null, new MetaInfo.FileInfo[] {fileInfoA, fileInfoB}, 50);
    assertEquals(11, storage.getPieceCount());

    storage = new Storage(null, null, new MetaInfo.FileInfo[] {fileInfoC}, 34);
    assertEquals(1, storage.getPieceCount());

    storage = new Storage(null, null, new MetaInfo.FileInfo[] {fileInfoC}, 30);
    assertEquals(2, storage.getPieceCount());
  }

  public void testPieceSize() {
    int[] tests = new int[] {13, 7, 14, 22};
    for (int test : tests) {
      Storage storage = new Storage(null, null, new MetaInfo.FileInfo[] {fileInfoA}, test);
      assertEquals(test, storage.getPieceLength());
    }
  }

  public void testPieceSizeWithIndex() {
    Storage storage = new Storage(null, null, new MetaInfo.FileInfo[] {fileInfoA}, 40);
    for (int i = 0; i < 8; i++) {
      assertEquals(40, storage.getPieceLength(i));
    }
    assertEquals(30, storage.getPieceLength(8));
  }

  public void testLastPieceSize() {
    int[] tests = new int[] {9, 4, 3, 360};
    for (int test : tests) {
      Storage storage = new Storage(null, null, new MetaInfo.FileInfo[] {fileInfoA}, test);
      assertEquals(350 % test, storage.getLastPieceLength());
    }
  }

  public void testLastPieceSameSizeAsOthers() {
    Storage storage = new Storage(null, null, new MetaInfo.FileInfo[] {fileInfoA}, 50);
    assertEquals(50, storage.getLastPieceLength());
    assertEquals(50, storage.getPieceLength());
    assertEquals(7, storage.getPieceCount());
    assertEquals(50, storage.getPieceLength(0));
    assertEquals(50, storage.getPieceLength(6));
  }
}
