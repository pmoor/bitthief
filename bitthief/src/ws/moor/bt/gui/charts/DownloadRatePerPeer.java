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
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import ws.moor.bt.stats.CounterRepository;
import ws.moor.bt.stats.CounterStatistics;
import ws.moor.bt.util.SystemTimeSource;
import ws.moor.bt.util.TimeSource;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;

/**
 * TODO(pmoor): Javadoc
 */
public class DownloadRatePerPeer extends JPanel implements Maintainable {

  private final CounterRepository counterRepository;

  private final JFreeChart chart;

  private TimeSource timeSource = SystemTimeSource.INSTANCE;

  public DownloadRatePerPeer(CounterRepository counterRepository) {
    super();
    this.counterRepository = counterRepository;

    chart = createChart();
    setLayout(new BorderLayout());
    add(new ChartPanel(chart), BorderLayout.CENTER);
  }

  private JFreeChart createChart() {

    JFreeChart chart = ChartFactory.createBarChart(
        "Top 20 Download Rates",
        "Remote IP",
        "KB/s",
        createDataset(),
        PlotOrientation.VERTICAL,
        false,
        false,
        false);
    chart.setBackgroundPaint(Color.white);

    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);

    CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setCategoryLabelPositions(
        CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 2.0)
    );

    return chart;
  }

  private DefaultCategoryDataset createDataset() {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    long now = timeSource.getTime();

    Map<Double, String> rateToKey = new TreeMap<Double, String>();
    for (String key : counterRepository.getKeys("network.rawbytes.in")) {
      CounterStatistics statistics =
          counterRepository.getStatistics("network.rawbytes.in", key);
      double rate = statistics.getValueAt(now) - statistics.getValueAt(now - 60 * 1000);
      rate /= 60 * 1000;
      rateToKey.put(-rate, key);
    }


    for (Map.Entry<Double, String> entry : rateToKey.entrySet()) {
      dataset.addValue(-entry.getKey(), "dl rate", entry.getValue());
      if (dataset.getColumnCount() >= 20) {
        break;
      }
    }

    return dataset;
  }

  public void maintain() {
    chart.getCategoryPlot().setDataset(createDataset());
  }
}
