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

/**
 * TODO(pmoor): Javadoc
 */
public class AdjustableTimeSource implements TimeSource {

  private long currentTime;

  public void increaseTime(long delta) {
    assertDeltaNonNegative(delta);
    currentTime += delta;
  }

  private void assertDeltaNonNegative(long delta) {
    if (delta < 0) {
      throw new IllegalArgumentException("delta must be non-negative");
    }
  }

  public long getTime() {
    return currentTime;
  }

  public void setTime(long time) {
    assertTimeNotInPast(time);
    currentTime = time;
  }

  private void assertTimeNotInPast(long time) {
    if (time < currentTime) {
      throw new IllegalArgumentException("cannot move clock backwards");
    }
  }
}
