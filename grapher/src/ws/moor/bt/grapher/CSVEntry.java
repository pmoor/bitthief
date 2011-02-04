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

import java.util.Date;

/**
 * TODO(pmoor): Javadoc
 */
public class CSVEntry {

  private final String name;
  private final long time;
  private final long value;

  public CSVEntry(String name, long time, long value) {
    this.name = name;
    this.time = time;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(name);
    builder.append("\t");
    builder.append(new Date(time));
    builder.append("\t");
    builder.append(value);
    return builder.toString();
  }

  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final CSVEntry entry = (CSVEntry) o;
    return time == entry.time && value == entry.value && name.equals(entry.name);
  }

  public int hashCode() {
    int result = name.hashCode();
    result = 29 * result + (int) (time ^ (time >>> 32));
    return 29 * result + (int) (value ^ (value >>> 32));
  }

  public Date getDate() {
    return new Date(time);
  }

  public long getTime() {
    return time;
  }

  public long getValue() {
    return value;
  }
}
