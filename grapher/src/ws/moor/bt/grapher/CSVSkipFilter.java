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
public class CSVSkipFilter extends CSVFilter {

  private long lastTime = 0;
  private final long minInterval;

  public CSVSkipFilter(CSVStream stream, long minInterval) {
    super(stream);
    this.minInterval = minInterval;
  }

  protected boolean accept(CSVEntry entry) {
    long delta = entry.getTime() - lastTime;
    if (delta < minInterval) {
      return false;
    } else {
      lastTime = entry.getTime();
      return true;
    }
  }
}
