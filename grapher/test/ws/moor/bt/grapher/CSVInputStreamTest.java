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

package ws.moor.bt.grapher;

import ws.moor.bt.util.ExtendedTestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * TODO(pmoor): Javadoc
 */
public class CSVInputStreamTest extends ExtendedTestCase {

  public void testAllFine() throws IOException {
    String src = "A\t47\t8\n" +
                 "B\t18\t2\n" +
                 "C\t88\t7\n";
    CSVInputStream stream =
        new CSVInputStream(new ByteArrayInputStream(src.getBytes()));
    assertTrue(stream.hasMoreEntries());
    assertEquals(new CSVEntry("A", 47, 8), stream.nextEntry());
    assertTrue(stream.hasMoreEntries());
    assertEquals(new CSVEntry("B", 18, 2), stream.nextEntry());
    assertTrue(stream.hasMoreEntries());
    assertEquals(new CSVEntry("C", 88, 7), stream.nextEntry());
    assertFalse(stream.hasMoreEntries());
  }
  
  public void testIncomplete() throws IOException {
    String src = "A\t47\t8\n" +
                 "B\t18\n" +
                 "C\t88\t7\n";
    CSVInputStream stream =
        new CSVInputStream(new ByteArrayInputStream(src.getBytes()));
    assertTrue(stream.hasMoreEntries());
    assertEquals(new CSVEntry("A", 47, 8), stream.nextEntry());
    assertTrue(stream.hasMoreEntries());
    assertEquals(new CSVEntry("C", 88, 7), stream.nextEntry());
    assertFalse(stream.hasMoreEntries());
  }

  public void testDouble() throws IOException {
    String src = "A\t47\t8.4\n" +
                 "B\t18\n" +
                 "C\t88\t7.2\n";
    CSVInputStream stream =
        new CSVInputStream(new ByteArrayInputStream(src.getBytes()));
    assertTrue(stream.hasMoreEntries());
    assertEquals(new CSVEntry("A", 47, 8400), stream.nextEntry());
    assertTrue(stream.hasMoreEntries());
    assertEquals(new CSVEntry("C", 88, 7200), stream.nextEntry());
    assertFalse(stream.hasMoreEntries());
  }
}
