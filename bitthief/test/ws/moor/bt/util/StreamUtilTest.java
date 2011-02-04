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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * TODO(pmoor): Javadoc
 */
public class StreamUtilTest extends ExtendedTestCase {

  public void testCopy() throws IOException {
    assertCorrectStringCopy("Hello World!");
  }

  public void testCopyOfLongString() throws IOException {
    StringBuilder testBuilder = new StringBuilder();
    for (int i = 0; i < 64 * 128; i++) {
      testBuilder.append(i);
      testBuilder.append("fancy string");
    }
    assertCorrectStringCopy(testBuilder.toString());
  }

  public void testCopyOfShortString() throws IOException {
    assertCorrectStringCopy("");
  }

  private void assertCorrectStringCopy(String testString) throws IOException {
    InputStream input = new ByteArrayInputStream(testString.getBytes());
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    StreamUtil.copy(input, output);
    assertEquals(testString, output.toString());
  }
}
