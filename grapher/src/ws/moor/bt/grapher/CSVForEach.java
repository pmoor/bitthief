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

import java.util.HashSet;
import java.util.Set;

public class CSVForEach {

  private final CSVStream stream;
  private Set<Listener> listeners = new HashSet<Listener>();

  public CSVForEach(CSVStream stream) {
    this.stream = stream;
  }

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void go() {
    while (stream.hasMoreEntries()) {
      CSVEntry entry = stream.nextEntry();
      for (Listener listener : listeners) {
        listener.process(entry);
      }
    }
  }

  public void close() {
    stream.close();
  }

  public static interface Listener {
    public void process(CSVEntry entry);
  }
}
