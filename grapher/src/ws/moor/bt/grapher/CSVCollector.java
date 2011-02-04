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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class CSVCollector {

  List<CSVEntry> list = new ArrayList<CSVEntry>();

  public CSVCollector(CSVStream stream) {
    while (stream.hasMoreEntries()) {
      list.add(stream.nextEntry());
    }
    Collections.sort(list, new Comparator<CSVEntry>() {
      public int compare(CSVEntry o1, CSVEntry o2) {
        if (o1.getTime() < o2.getTime()) {
          return -1;
        } else if (o1.getTime() > o2.getTime()) {
          return +1;
        } else {
          return 0;
        }
      }
    });
  }

  public CSVStream getStream() {
    return new CSVIterableStream(list);
  }

  public int size() {
    return list.size();
  }

  public CSVEntry getFirst() {
    return list.get(0);
  }

  public CSVEntry getLast() {
    return list.get(list.size() - 1);
  }
}
