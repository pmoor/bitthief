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

package ws.moor.bt.torrent;

import ws.moor.bt.util.ExtendedTestCase;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * TODO(pmoor): Javadoc
 */
public class MetaInfoTest extends ExtendedTestCase {

  public void testConstructFile() {
    MetaInfo.FileInfo fileInfo = new MetaInfo.FileInfo(new String[] {"foo", "bar"}, 100);
    assertEquals(100, fileInfo.getLength());

    File parent = new File("/home/user");
    File expected = new File(new File(parent, "foo"), "bar");
    assertEquals(expected, fileInfo.constructFile(parent));
  }

  public void testPieceCount() throws MalformedURLException {
    MetaInfo.FileInfo fileInfoA = new MetaInfo.FileInfo(new String[] {"foo", "bar"}, 100);
    MetaInfo.FileInfo fileInfoB = new MetaInfo.FileInfo(new String[] {"foo", "bar2"}, 100);
    MetaInfo.FileInfo[] infos = new MetaInfo.FileInfo[] {fileInfoA, fileInfoB};

    MetaInfo info = new MultiFileMetaInfo(
        HashTest.randomHash(),
        new URL("http://moor.ws/"),
        null,
        new Date(),
        50,
        "dummy",
        getSomeHashes(4),
        infos);

    assertEquals(4, info.getPieceCount());
  }

  public void testDetermineRootDirectoryMultiFileCase() throws MalformedURLException {
    MetaInfo.FileInfo fileInfoA = new MetaInfo.FileInfo(new String[] {"foo", "bar"}, 200);
    MetaInfo.FileInfo[] infos = new MetaInfo.FileInfo[] {fileInfoA};
    MetaInfo info = new MultiFileMetaInfo(
        HashTest.randomHash(),
        new URL("http://moor.ws/"),
        null,
        new Date(),
        50,
        "directoryName",
        getSomeHashes(4),
        infos);
    assertEquals(new File("/root/directoryName"), info.determineRootDirectory(new File("/root")));
  }

  public void testDetermineRootDirectorySingleFileCase() throws MalformedURLException {
    MetaInfo info = new SingleFileMetaInfo(
        HashTest.randomHash(),
        new URL("http://moor.ws/"),
        null,
        new Date(),
        50,
        "directoryName",
        getSomeHashes(4),
        200);
    assertEquals(new File("/root"), info.determineRootDirectory(new File("/root")));
  }

  private Hash[] getSomeHashes(int hashCount) {
    Hash[] hashes = new Hash[hashCount];
    for (int i = 0; i < hashCount; i++) {
      hashes[i] = HashTest.randomHash();
    }
    return hashes;
  }
}
