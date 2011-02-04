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

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * TODO(pmoor): Javadoc
 */
public class TotalBlocksPerPeer extends JPanel implements Maintainable {

  private final JFreeChart chart;
  private final CounterRepository counterRepository;

  public TotalBlocksPerPeer(CounterRepository counterRepository) {
    super();
    this.counterRepository = counterRepository;

    chart = createChart();
    setLayout(new BorderLayout());
    add(new ChartPanel(chart), BorderLayout.CENTER);
  }

  private JFreeChart createChart() {

    JFreeChart chart = ChartFactory.createStackedBarChart(
        "Top 20 Block Peers",
        "Remote IP",
        "Blocks",
        createDataset(),
        PlotOrientation.VERTICAL,
        true,
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

    Map<Long, String> rateToKey = new TreeMap<Long, String>();
    Collection<String> keys = new HashSet<String>();
    keys.addAll(counterRepository.getKeys("torrent.blocks.leecher"));
    keys.addAll(counterRepository.getKeys("torrent.blocks.seed"));
    for (String key : keys) {
      CounterStatistics leechStats =
          counterRepository.getStatistics("torrent.blocks.leecher", key);
      CounterStatistics seedStats =
          counterRepository.getStatistics("torrent.blocks.seed", key);

      long blocks = leechStats.latestValue() + seedStats.latestValue();
      rateToKey.put(-blocks, key);
    }


    for (String key : rateToKey.values()) {
      CounterStatistics leechStats =
          counterRepository.getStatistics("torrent.blocks.leecher", key);
      CounterStatistics seedStats =
          counterRepository.getStatistics("torrent.blocks.seed", key);

      dataset.addValue(seedStats.latestValue(), "seed", key);
      dataset.addValue(leechStats.latestValue(), "leecher", key);
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
