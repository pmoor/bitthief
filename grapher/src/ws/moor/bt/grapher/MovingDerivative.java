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

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;

import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class MovingDerivative {

  public static TimeSeries createDerivative(TimeSeries series, long miliseconds) {
    TimeSeries result = new TimeSeries(series.getKey().toString(), series.getTimePeriodClass());

    List<TimeSeriesDataItem> dataItems = series.getItems();

    int i = 0;
    do {
      TimeSeriesDataItem now = dataItems.get(i);
      long then = now.getPeriod().getSerialIndex() + miliseconds;

      int j = i + 1;
      while (j < dataItems.size() && dataItems.get(j).getPeriod().getSerialIndex() < then) {
        j++;
      }
      if (j == dataItems.size()) {
        j = dataItems.size() - 1;
      }

      TimeSeriesDataItem future = dataItems.get(j);

      double derivative = 1000.0 * (future.getValue().doubleValue() - now.getValue().doubleValue());
      derivative /= future.getPeriod().getSerialIndex() - now.getPeriod().getSerialIndex();
      result.add(now.getPeriod(), derivative);
      i++;
    } while (i < dataItems.size());

    return result;
  }
}
