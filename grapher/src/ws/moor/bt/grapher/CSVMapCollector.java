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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO(pmoor): Javadoc
 */
public class CSVMapCollector {

  Map<String, List<CSVEntry>> map = new HashMap<String, List<CSVEntry>>();

  public CSVMapCollector(CSVStream stream) {
    while (stream.hasMoreEntries()) {
      CSVEntry entry = stream.nextEntry();
      List<CSVEntry> list = map.get(entry.getName());
      if (list == null) {
        list = new ArrayList<CSVEntry>();
        map.put(entry.getName(), list);
      }
      list.add(entry);
    }
  }

  public CSVStream getStream(String name) {
    List<CSVEntry> list = map.get(name);
    return list != null ? new CSVIterableStream(list) : null;
  }

  public List<String> getAvailableStreams() {
    return new ArrayList<String>(map.keySet());
  }
}
