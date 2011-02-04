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

import org.apache.commons.codec.binary.Hex;
import ws.moor.bt.util.ExtendedTestCase;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;

/**
 * TODO(pmoor): Javadoc
 */
public class MetaInfoBuilderTest extends ExtendedTestCase {

  byte[] singleFile =
    ("d" +
      "8:announce20:http://announce.com/" +
      "7:comment2:Hi" +
      "13:creation datei1118418767e" +
      "4:infod" +
        "6:lengthi500e" +
        "4:name15:stentz-src-i386" +
        "12:piece lengthi262144e" +
        "6:pieces20:01234567890123456789" +
      "e" +
    "e").getBytes();

  byte[] multiFile =
    ("d" +
      "8:announce20:http://announce.com/" +
      "13:creation datei1118418767e" +
      "4:infod" +
        "5:filesl" +
          "d6:lengthi55e4:pathl3:fooee" +
          "d6:lengthi88e4:pathl3:foo3:baree" +
        "e" +
        "4:name15:stentz-src-i386" +
        "12:piece lengthi262144e" +
        "6:pieces20:01234567890123456789" +
      "e" +
    "e").getBytes();

  public void testSingleFileConstruction() throws Exception {
    MetaInfo result = MetaInfoBuilder.fromStream(new ByteArrayInputStream(singleFile));
    assertEquals(new URL("http://announce.com/"), result.getAnnounceUrl());
    assertEquals("Hi", result.getComment());
    assertEquals(new Date(1118418767 * 1000l), result.getCreationDate());
    assertEquals(new Hash(Hex.decodeHex("ee137f35845a6929ba2edaefab92dbf38df5f0d7".toCharArray())),
        result.getInfoHash());
    assertEquals("stentz-src-i386", result.getName());
    assertArrayEquals(new Hash[] {new Hash("01234567890123456789".getBytes())}, result.getPieceHashes());
    assertEquals(262144, result.getPieceLength());
    assertInstanceof(SingleFileMetaInfo.class, result);
    assertEquals(500, result.getTotalLength());
    MetaInfo.FileInfo[] fileInfos = result.getFileInfos();
    assertEquals(1, fileInfos.length);
    assertEquals(500, fileInfos[0].getLength());
    assertEquals(1, fileInfos[0].getPath().length);
    assertEquals("stentz-src-i386", fileInfos[0].getPath()[0]);
  }

  public void testMultiFileConstruction() throws Exception {
    MetaInfo result = MetaInfoBuilder.fromStream(new ByteArrayInputStream(multiFile));
    assertEquals(new URL("http://announce.com/"), result.getAnnounceUrl());
    assertNull(result.getComment());
    assertEquals(new Date(1118418767 * 1000l), result.getCreationDate());
    assertEquals(new Hash(Hex.decodeHex("2e70c05a6831c25c461c1358d33d8d94291fa951".toCharArray())),
        result.getInfoHash());
    assertEquals("stentz-src-i386", result.getName());
    assertArrayEquals(new Hash[] {new Hash("01234567890123456789".getBytes())}, result.getPieceHashes());
    assertEquals(262144, result.getPieceLength());
    assertInstanceof(MultiFileMetaInfo.class, result);
    assertEquals(143, result.getTotalLength());

    MetaInfo.FileInfo[] fileInfos = result.getFileInfos();
    assertEquals(2, fileInfos.length);
    assertEquals(55, fileInfos[0].getLength());
    assertEquals(88, fileInfos[1].getLength());
    assertEquals(1, fileInfos[0].getPath().length);
    assertEquals("foo", fileInfos[0].getPath()[0]);
    assertEquals(2, fileInfos[1].getPath().length);
    assertEquals("foo", fileInfos[1].getPath()[0]);
    assertEquals("bar", fileInfos[1].getPath()[1]);
  }

  public void testSingleFileEquals() throws Exception {
    MetaInfo result = MetaInfoBuilder.fromStream(new ByteArrayInputStream(singleFile));
    MetaInfo expectedMetaInfo = new SingleFileMetaInfo(
        new Hash(Hex.decodeHex("c7f50c5206b003782189bb571d590e1450369be4".toCharArray())),
        new URL("http://announce.com/"),
        "Hi",
        new Date(1118418767 * 1000l),
        262144,
        "stentz-src-i386",
        new Hash[] {null},
        500);
    assertEquals(expectedMetaInfo, result);
  }

  public void testMultiFileEquals() throws Exception {
    MetaInfo result = MetaInfoBuilder.fromStream(new ByteArrayInputStream(multiFile));
    MetaInfo expectedMetaInfo = new MultiFileMetaInfo(
        new Hash(Hex.decodeHex("c7f50c5206b003782189bb571d590e1450369be4".toCharArray())),
        new URL("http://announce.com/"),
        null,
        new Date(1118418767 * 1000l),
        262144,
        "stentz-src-i386",
        new Hash[] {null},
        new MetaInfo.FileInfo[] {
            new MetaInfo.FileInfo(
                new String[] { "foo" }, 55 ),
            new MetaInfo.FileInfo(
                new String[] { "foo", "bar" }, 88 )});
    assertEquals(expectedMetaInfo, result);
  }
}
