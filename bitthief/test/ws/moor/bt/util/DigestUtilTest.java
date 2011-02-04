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

package ws.moor.bt.util;

import org.apache.commons.codec.binary.Hex;

/**
 * TODO(pmoor): Javadoc
 */
public class DigestUtilTest extends ExtendedTestCase {

  /**
   * expected strings were generated using the command line tool sha1sum
   */
  public void testSha1() {
    String testString = "Hello World";
    String expected = "0a4d55a8d778e5022fab701977c5d840bbc486d0";
    assertSha1Correct(expected, testString);

    testString = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. " +
        "Quisque turpis justo, porttitor ut, placerat quis, mattis vitae metus.";
    expected = "71247e6a738b0ebb9c31938ccf5e2ef6283874ed";
    assertSha1Correct(expected, testString);
  }

  public void testSha1WithOffset() {
    String testString = "123Hello World321";
    String expected = "0a4d55a8d778e5022fab701977c5d840bbc486d0";
    byte[] digest = DigestUtil.sha1(testString.getBytes(), 3, testString.getBytes().length - 6);
    assertEquals(expected, new String(Hex.encodeHex(digest)));

    testString = "12345678Lorem ipsum dolor sit amet, consectetuer adipiscing elit. " +
        "Quisque turpis justo, porttitor ut, placerat quis, mattis vitae metus.123";
    expected = "71247e6a738b0ebb9c31938ccf5e2ef6283874ed";
    digest = DigestUtil.sha1(testString.getBytes(), 8, testString.getBytes().length - 11);
    assertEquals(expected, new String(Hex.encodeHex(digest)));
  }

  private void assertSha1Correct(String expected, String testString) {
    byte[] digest = DigestUtil.sha1(testString.getBytes());
    assertEquals(expected, new String(Hex.encodeHex(digest)));
  }
}
