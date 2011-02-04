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
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import ws.moor.bt.stats.CounterRepository;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;

public class ConnectionTypes extends JPanel implements Maintainable {

  private final JFreeChart chart;
  private final CounterStatisticsDataSource seederSource;
  private final CounterStatisticsDataSource leecherSource;

  public ConnectionTypes(CounterRepository counterRepository) {
    super();

    seederSource = new CounterStatisticsDataSource(
        counterRepository.getStatistics("network.connections.seed")) {
      protected double getValueToShowAt(long time) {
        return statistics.getValueAt(time);
      }
    };
    leecherSource = new CounterStatisticsDataSource(
        counterRepository.getStatistics("network.connections.leecher")) {
      protected double getValueToShowAt(long time) {
        return statistics.getValueAt(time);
      }
    };

    chart = createChart(getTimeSeriesCollection());
    setLayout(new BorderLayout());
    ChartPanel panel = new ChartPanel(chart, false, true, false, false, false);
    panel.setMouseZoomable(false);
    add(panel, BorderLayout.CENTER);
  }

  private TimeSeriesCollection getTimeSeriesCollection() {
    TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(seederSource.getTimeSeries());
    dataset.addSeries(leecherSource.getTimeSeries());
    return dataset;
  }

  private JFreeChart createChart(XYDataset dataset) {
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        "Connection Types",
        "Time",
        "Connections",
        dataset,
        true,
        false,
        false);
    chart.setBackgroundPaint(Color.white);

    XYPlot plot = (XYPlot) chart.getPlot();
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
    leecherSource.update();
    seederSource.update();
  }
}
