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

import java.io.File;
import java.util.Random;

/**
 * TODO(pmoor): Javadoc
 */
public class VirtualFileRepositoryTest extends ExtendedTestCase {

  private FileRepository repository;
  private File fileA;
  private File fileB;
  private File fileC;

  protected void setUp() throws Exception {
    super.setUp();
    repository = new VirtualFileRepository();
    fileA = new File("A");
    fileB = new File("B");
    fileC = new File("C");
  }

  public void testReadFromNowehere() {
    byte[] buffer = new byte[128];
    byte[] buffer2 = new byte[128];

    repository.read(fileA, 50, buffer, 0, 128);
    assertArrayEquals(buffer2, buffer);

    repository.read(fileB, 0, buffer, 30, 50);
    assertArrayEquals(buffer2, buffer);

    repository.read(fileC, 80, buffer, 5, 5);
    assertArrayEquals(buffer2, buffer);
  }

  public void testRepeatedRead() {
    byte[] buffer = new byte[128];
    byte[] buffer2 = new byte[128];

    repository.read(fileA, 0, buffer, 0, 128);
    assertArrayEquals(buffer2, buffer);

    new Random().nextBytes(buffer);
    repository.write(buffer, 0, fileA, 64, 128);

    System.arraycopy(buffer, 64, buffer2, 0, 64);
    repository.read(fileA, 128, buffer, 0, 128);
    assertArrayEquals(buffer2, buffer);
  }

  public void testWriteAndRead() {
    byte[] buffer = new byte[128];
    rnd.nextBytes(buffer);

    byte[] buffer2 = new byte[128];

    repository.write(buffer, 0, fileA, 0, 128);
    repository.read(fileA, 0, buffer2, 0, 128);
    assertArrayEquals(buffer, buffer2);

    repository.write(buffer, 0, fileB, 32, 64);
    repository.read(fileB, 32, buffer2, 0, 64);
    assertArrayEquals(buffer, buffer2);
  }
}
