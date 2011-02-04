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
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import ws.moor.bt.stats.CounterRepository;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;

public class BlockOrigin extends JPanel implements Maintainable {

  private final CounterRepository counterRepository;

  private final JFreeChart chart;

  public BlockOrigin(CounterRepository counterRepository) {
    super();
    this.counterRepository = counterRepository;

    chart = createChart(getDataSet());
    setLayout(new BorderLayout());
    ChartPanel panel = new ChartPanel(chart, false, true, false, false, false);
    panel.setMouseZoomable(false);
    add(panel, BorderLayout.CENTER);
  }

  private PieDataset getDataSet() {
    DefaultPieDataset dataset = new DefaultPieDataset();
    dataset.setValue("from Leechers", counterRepository.getStatistics("torrent.blocks.leecher").latestValue());
    dataset.setValue("from Seeders", counterRepository.getStatistics("torrent.blocks.seed").latestValue());
    return dataset;
  }

  private JFreeChart createChart(PieDataset dataset) {

    JFreeChart chart = ChartFactory.createPieChart3D(
        "Block Origin",
        dataset,
        false, false, false);
    chart.setBackgroundPaint(Color.white);

    ((PiePlot3D) chart.getPlot()).setSectionPaint(0, Color.YELLOW);
    ((PiePlot3D) chart.getPlot()).setSectionPaint(0, Color.GREEN);

    return chart;
  }

  public void maintain() {
    ((PiePlot3D) chart.getPlot()).setDataset(getDataSet());
  }
}
