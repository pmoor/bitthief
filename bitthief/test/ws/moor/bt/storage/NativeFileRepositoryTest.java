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

import ws.moor.bt.util.ExtendedTestCase;
import ws.moor.bt.util.StreamUtil;
import ws.moor.bt.util.TestUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * TODO(pmoor): Javadoc
 */
public class NativeFileRepositoryTest extends ExtendedTestCase {

  private FileRepository fileRepository;
  private File fileA, fileB, fileC;

  protected void setUp() throws Exception {
    super.setUp();
    fileRepository = new NativeFileRepository();
    fileA = TestUtil.getTempFile();
    fileB = TestUtil.getTempFile();
    fileC = TestUtil.getTempFile();
  }

  public void testWrites() throws IOException {
    byte[] buffer = new byte[128];
    rnd.nextBytes(buffer);

    fileRepository.write(buffer, 0, fileA, 0, 128);
    fileRepository.write(buffer, 0, fileB, 64, 128);
    fileRepository.write(buffer, 64, fileC, 0, 64);

    assertEquals(128, fileA.length());
    assertArrayEquals(buffer, fileContent(fileA));

    assertEquals(192, fileB.length());
    byte[] buffer2 = new byte[192];
    System.arraycopy(buffer, 0, buffer2, 64, 128);
    assertArrayEquals(buffer2, fileContent(fileB));

    buffer2 = new byte[64];
    System.arraycopy(buffer, 64, buffer2, 0, 64);
    assertEquals(64, fileC.length());
    assertArrayEquals(buffer2, fileContent(fileC));
  }

  public void testMultipleWrites() throws IOException {
    byte[] buffer = new byte[128];
    rnd.nextBytes(buffer);

    fileRepository.write(buffer, 0, fileA, 0, 128);
    fileRepository.write(buffer, 0, fileA, 64, 128);
    fileRepository.write(buffer, 0, fileA, 128, 128);

    assertEquals(256, fileA.length());
    byte[] buffer2 = new byte[256];
    System.arraycopy(buffer, 0, buffer2, 0, 64);
    System.arraycopy(buffer, 0, buffer2, 64, 64);
    System.arraycopy(buffer, 0, buffer2, 128, 128);
    assertArrayEquals(buffer2, fileContent(fileA));
  }

  public void testEmptyReads() throws IOException {
    byte[] buffer = new byte[128];
    fileRepository.read(fileA, 128, buffer, 0, 128);
    assertEquals(256, fileA.length());
    assertArrayEquals(new byte[256], fileContent(fileA));
  }

  public void testWriteRead() throws IOException {
    byte[] buffer = new byte[128];
    rnd.nextBytes(buffer);
    byte[] buffer2 = new byte[128];

    fileRepository.write(buffer, 0, fileC, 32, 128);
    fileRepository.read(fileC, 32, buffer2, 0, 128);
    assertEquals(160, fileC.length());
    assertArrayEquals(buffer2, buffer);
  }

  private byte[] fileContent(File file) throws IOException {
    InputStream is = new FileInputStream(file);
    ByteArrayOutputStream os = new ByteArrayOutputStream((int) file.length());
    StreamUtil.copy(is, os);
    return os.toByteArray();
  }
}
