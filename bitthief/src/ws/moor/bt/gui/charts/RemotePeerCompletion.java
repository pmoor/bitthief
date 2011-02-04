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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.ui.RectangleInsets;
import ws.moor.bt.downloader.TorrentDownload;
import ws.moor.bt.network.BitTorrentConnection;
import ws.moor.bt.storage.BitField;
import ws.moor.bt.util.CollectionUtils;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class RemotePeerCompletion extends JPanel implements Maintainable {

  private final JFreeChart chart;
  private final TorrentDownload download;

  public RemotePeerCompletion(TorrentDownload download) {
    super();
    this.download = download;

    chart = createChart(getDataSet());
    setLayout(new BorderLayout());
    ChartPanel panel = new ChartPanel(chart, false, true, false, false, false);
    panel.setMouseZoomable(false);
    add(panel, BorderLayout.CENTER);
  }

  private HistogramDataset getDataSet() {
    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);

    List<Double> values =
        CollectionUtils.mapNullRemoves(
            download.getAllValidConnections(),
            new CollectionUtils.Function<BitTorrentConnection, Double>() {
          public Double evaluate(BitTorrentConnection connection) {
            BitField bitField = connection.getRemoteBitField();
            if (bitField == null || !connection.isOpen()) {
              return null;
            }
            return 100.0 * bitField.getAvailablePieceCount() / bitField.getPieceCount();
          }
        });

    dataset.addSeries("Completion", CollectionUtils.toArray(values), 50, 0.0, 100.0);
    return dataset;
  }

  private JFreeChart createChart(HistogramDataset dataset) {

    JFreeChart chart = ChartFactory.createHistogram(
        "Remote Peer Completion",
        "Completion %",
        "Peers",
        dataset,
        PlotOrientation.VERTICAL,
        false, false, false);
    chart.setBackgroundPaint(Color.white);

    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);
    plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
    plot.setRangeCrosshairVisible(true);

    plot.getDomainAxis().setAutoRange(false);
    plot.getDomainAxis().setRange(0.0, 100.0);

    return chart;
  }

  public void maintain() {
    chart.getXYPlot().setDataset(getDataSet());
  }
}
