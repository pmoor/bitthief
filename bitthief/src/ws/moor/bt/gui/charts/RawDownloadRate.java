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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import ws.moor.bt.stats.CounterRepository;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;

/**
 * TODO(pmoor): Javadoc
 */
public class RawDownloadRate extends JPanel implements Maintainable {

  private final JFreeChart chart;
  private final CounterStatisticsDataSource dataSource;

  public RawDownloadRate(CounterRepository counterRepository) {
    super();
    dataSource = new CounterStatisticsDataSource(
        counterRepository.getStatistics("network.rawbytes.in")) {
      protected double getValueToShowAt(long time) {
        return statistics.getIncreaseAt(time);
      }
    };
    chart = createChart(getTimeSeriesCollection());
    setLayout(new BorderLayout());
    add(new ChartPanel(chart), BorderLayout.CENTER);
  }

  private TimeSeriesCollection getTimeSeriesCollection() {
    TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(dataSource.getTimeSeries());
    dataset.addSeries(createAverageSeries(dataSource.getTimeSeries()));
    return dataset;
  }

  private TimeSeries createAverageSeries(TimeSeries timeSeries) {
    return MovingAverage.createMovingAverage(timeSeries, "5 Minute Average", 5 * 60, 0);
  }

  private JFreeChart createChart(XYDataset dataset) {
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        "Download Rate",
        "Time",
        "KB/s",
        dataset,
        false,
        false,
        false);
    chart.setBackgroundPaint(Color.white);

    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);
    plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
    plot.setDomainCrosshairVisible(true);
    plot.setRangeCrosshairVisible(true);

    DateAxis axis = (DateAxis) plot.getDomainAxis();
    axis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));

    return chart;
  }

  public void maintain() {
    dataSource.update();
    TimeSeriesCollection collection = (TimeSeriesCollection) chart.getXYPlot().getDataset();
    collection.removeSeries(1);
    collection.addSeries(createAverageSeries(dataSource.getTimeSeries()));
  }
}
