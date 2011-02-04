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

/**
 * TODO(pmoor): Javadoc
 */
public abstract class CSVFilter implements CSVStream {

  private final CSVStream stream;

  private boolean started = false;
  private CSVEntry nextEntry;

  public CSVFilter(CSVStream stream) {
    this.stream = stream;
  }

  public CSVEntry nextEntry() {
    startIfNeeded();
    CSVEntry entry = nextEntry;
    fetchNewEntry();
    return entry;
  }

  private void fetchNewEntry() {
    nextEntry = null;
    while (stream.hasMoreEntries()) {
      nextEntry = stream.nextEntry();
      if (accept(nextEntry)) {
        return;
      }
    }
    nextEntry = null;
  }

  protected abstract boolean accept(CSVEntry entry);

  public boolean hasMoreEntries() {
    startIfNeeded();
    return nextEntry != null;
  }

  private void startIfNeeded() {
    if (!started) {
      started = true;
      fetchNewEntry();
    }
  }

  public void close() {
    stream.close();
  }
}
