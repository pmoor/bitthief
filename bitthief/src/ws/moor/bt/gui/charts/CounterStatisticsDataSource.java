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

package ws.moor.bt.gui.charts;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import ws.moor.bt.stats.CounterStatistics;
import ws.moor.bt.util.SystemTimeSource;
import ws.moor.bt.util.TimeSource;

import java.util.Date;

/**
 * TODO(pmoor): Javadoc
 */
public class CounterStatisticsDataSource {

  protected final CounterStatistics statistics;

  private final TimeSeries timeSeries;

  private long latestTime;
  private long lastFullUpdate;

  private static final int FULL_UPDATE_TIMEOUT = 5 * 60 * 1000;

  private TimeSource timeSource = SystemTimeSource.INSTANCE;

  public CounterStatisticsDataSource(CounterStatistics statistics) {
    this.statistics = statistics;
    timeSeries = new TimeSeries(statistics.getName(), Second.class);
    latestTime = 0;
  }

  public TimeSeries getTimeSeries() {
    return timeSeries;
  }

  public void update() {
    long now = timeSource.getTime();
    if (now - lastFullUpdate > FULL_UPDATE_TIMEOUT) {
      clearTimeSeries();
      lastFullUpdate = now;
    }
    updateTimeSeries();
  }

  private void clearTimeSeries() {
    timeSeries.clear();
    latestTime = statistics.oldestData();
  }

  private void updateTimeSeries() {
    long now = timeSource.getTime();
    // don't show last values, as they might be significantly lower than older ones
    now -= statistics.intervalResolutionAt(now);
    while (latestTime < now) {
      timeSeries.add(new Second(new Date(latestTime)), getValueToShowAt(latestTime));
      latestTime += statistics.intervalResolutionAt(latestTime);
    }
  }

  protected double getValueToShowAt(long time) {
    return statistics.getValueAt(time);
  }
}
