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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * TODO(pmoor): Javadoc
 */
public class CSVInputStream implements CSVStream {

  private final BufferedReader reader;

  private CSVEntry nextEntry;

  public CSVInputStream(InputStream stream) {
    reader = new BufferedReader(new InputStreamReader(stream));
    fetchNextEntry();
  }

  public CSVEntry nextEntry() {
    assertNextEntryNotNull();
    CSVEntry entry = nextEntry;
    fetchNextEntry();
    return entry;
  }

  private void fetchNextEntry() {
    nextEntry = null;
    do {
      String line = null;
      try {
        line = reader.readLine();
      } catch (IOException e) {
        return;
      }
      if (line == null) {
        return;
      }
      StringTokenizer st = new StringTokenizer(line.trim());
      if (!st.hasMoreTokens()) {
        continue;
      }
      String name = new String(st.nextToken()).intern();
      if (!st.hasMoreTokens()) {
        continue;
      }
      String time = st.nextToken();
      if (!st.hasMoreTokens()) {
        continue;
      }
      String value = st.nextToken();
      nextEntry = new CSVEntry(name, Long.parseLong(time), getValue(value));
    } while (nextEntry == null);
  }

  private long getValue(String value) {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      return (long) (Double.parseDouble(value) * 1000.0);
    }
  }

  private void assertNextEntryNotNull() {
    if (nextEntry == null) {
      throw new IllegalStateException("no more entries");
    }
  }

  public boolean hasMoreEntries() {
    return nextEntry != null;
  }

  public void close() {
    try {
      reader.close();
    } catch (IOException e) {}
  }
}
