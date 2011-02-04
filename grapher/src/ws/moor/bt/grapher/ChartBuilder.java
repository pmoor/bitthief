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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.SimpleDateFormat;

/**
 * TODO(pmoor): Javadoc
 */
public class ChartBuilder {

  private final String chartName;
  private final TimeSeriesCollection timeSeries;

  private String valueAxisLabel = "Value";
  private String timeAxisLabel = "Time";
  private boolean showLegend = true;

  public ChartBuilder(String chartName) {
    this.chartName = chartName;
    timeSeries = new TimeSeriesCollection();
  }

  public void addDataset(String name, CSVStream stream) {
    TimeSeries series = constructSeries(name, stream);
    timeSeries.addSeries(series);
  }

  public void addDerivedDataset(String name, CSVStream stream) {
    TimeSeries series = constructSeries(name, stream);
    timeSeries.addSeries(MovingDerivative.createDerivative(series, 300 * 1000));
  }

  public void showLegend(boolean show) {
    showLegend = show;
  }

  private TimeSeries constructSeries(String name, CSVStream stream) {
    TimeSeries series = new TimeSeries(name, FixedMillisecond.class);
    while (stream.hasMoreEntries()) {
      CSVEntry entry = stream.nextEntry();
      series.addOrUpdate(new FixedMillisecond(entry.getDate()), entry.getValue());
    }
    return series;
  }

  public JFreeChart getChart() {
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        chartName,
        timeAxisLabel,
        valueAxisLabel,
        timeSeries,
        showLegend && (timeSeries.getSeriesCount() > 1),
        false,
        false);
    chart.setBackgroundPaint(Color.white);

    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.white);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);
    plot.setDomainCrosshairVisible(true);
    plot.setRangeCrosshairVisible(true);

    plot.getRenderer().setSeriesPaint(0, Color.black);
    plot.getRenderer().setSeriesStroke(0, new BasicStroke(1.0f));
    plot.getRenderer().setSeriesPaint(1, Color.black);
    plot.getRenderer().setSeriesStroke(1, new BasicStroke(2.0f));
    plot.getRenderer().setSeriesPaint(2, Color.black);
    plot.getRenderer().setSeriesStroke(2, new BasicStroke(0.1f));

    DateAxis axis = (DateAxis) plot.getDomainAxis();
    axis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));

    return chart;
  }

  public void setValueAxisLabel(String valueAxisLabel) {
    this.valueAxisLabel = valueAxisLabel;
  }

  public void setTimeAxisLabel(String timeAxisLabel) {
    this.timeAxisLabel = timeAxisLabel;
  }
}
