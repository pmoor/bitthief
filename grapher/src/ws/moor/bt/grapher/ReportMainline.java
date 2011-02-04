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

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * TODO(pmoor): Javadoc
 */
public class ReportMainline extends Report {

  private JFreeChart rawBytesChart;
  private JFreeChart trackerChart;
  private JFreeChart connectionChart;

  public ReportMainline(File csvFile, String name) throws FileNotFoundException {
    super(csvFile, name);
  }

  public int getAverageSeederCount() {
    return (int) getAverage("trackerSeeds");
  }

  public int getAverageLeecherCount() {
    return (int) getAverage("trackerPeers") - getAverageSeederCount();
  }

  private void buildCharts() {
    ChartBuilder rawBytesChartBuilder = new ChartBuilder("Raw Bytes");
    rawBytesChartBuilder.setValueAxisLabel("B/s");
    rawBytesChartBuilder.addDerivedDataset("In", collector.getStream("downTotal"));
    rawBytesChartBuilder.addDerivedDataset("Out", collector.getStream("upTotal"));
    rawBytesChart = rawBytesChartBuilder.getChart();

    if (collector.getStream("trackerPeers") != null) {
      ChartBuilder trackerChartBuilder = new ChartBuilder("Tracker");
      trackerChartBuilder.setValueAxisLabel("Peers");
      trackerChartBuilder.addDataset("Peers", collector.getStream("trackerPeers"));
      trackerChartBuilder.addDataset("Seeds", collector.getStream("trackerSeeds"));
      trackerChart = trackerChartBuilder.getChart();
    } else {
      trackerChart = null;
    }

    ChartBuilder connectionChartBuilder = new ChartBuilder("Connections");
    connectionChartBuilder.setValueAxisLabel("Connections");
    connectionChartBuilder.addDataset("Seeder", collector.getStream("numSeeds"));
    connectionChartBuilder.addDataset("Peers", collector.getStream("numPeers"));
    connectionChart = connectionChartBuilder.getChart();
  }

  public void create(OutputStream stream, File directory) throws IOException {
    buildCharts();
    directory.mkdirs();
    StringBuilder htmlFile = new StringBuilder();
    htmlFile.append("<html>\n");
    htmlFile.append("<h1 style=\"page-break-before: always;\">").append(getName()).append("</h1>\n");
    htmlFile.append("Running Time: ").append(getRunningTime() / 1000 / 60).append("min").append("<br/>\n");
    htmlFile.append("Total Down: ").append(getLastValue("downTotal") / 1024 / 1024).append("MB").append("<br/>\n");
    htmlFile.append("Total Up: ").append(getLastValue("upTotal") / 1024 / 1024).append("MB").append("<br/>\n");
    File rawbytefile = new File(directory, "rawbytes.png");
    ChartUtilities.saveChartAsPNG(rawbytefile, rawBytesChart, ReportMainline.WIDTH, ReportMainline.HEIGHT);
    appendHTMLForGraph(htmlFile, rawbytefile, "Raw Bytes", ReportMainline.WIDTH, ReportMainline.HEIGHT);
    File trackerfile = new File(directory, "tracker.png");
    if (trackerChart != null) {
      ChartUtilities.saveChartAsPNG(trackerfile, trackerChart, ReportMainline.WIDTH, ReportMainline.HEIGHT);
      appendHTMLForGraph(htmlFile, trackerfile, "Tracker", ReportMainline.WIDTH, ReportMainline.HEIGHT);
    }
    File connectionfile = new File(directory, "connections.png");
    ChartUtilities.saveChartAsPNG(connectionfile, connectionChart, ReportMainline.WIDTH, ReportMainline.HEIGHT);
    appendHTMLForGraph(htmlFile, connectionfile, "Connections", ReportMainline.WIDTH, ReportMainline.HEIGHT);

    stream.write(htmlFile.toString().getBytes());
  }

  public void create(File directory) throws IOException {
    directory.mkdirs();
    FileOutputStream fos = new FileOutputStream(new File(directory, getName() + ".html"));
    create(fos, directory);
    fos.close();
  }
}
