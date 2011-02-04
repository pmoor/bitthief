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
public class ReportBitThief extends Report {

  private JFreeChart piecesChart;
  private JFreeChart rawBytesChart;
  private JFreeChart trackerChart;
  private JFreeChart connectionChart;
  private JFreeChart blockChart;

  public ReportBitThief(File csvFile, String name) throws FileNotFoundException {
    super(csvFile, name);
  }

  public int getAverageSeederCount() {
    return (int) getAverage("tracker.seeds");
  }

  public int getAverageLeecherCount() {
    return (int) getAverage("tracker.leechers");
  }

  private void buildCharts() {
    ChartBuilder piecesChartBuilder = new ChartBuilder("Pieces");
    piecesChartBuilder.setValueAxisLabel("Pieces");
    addDatasetIfAvailable("Valid Pieces", "torrent.pieces.valid", piecesChartBuilder);
    addDatasetIfAvailable("Pending Pieces", "torrent.pieces.pending", piecesChartBuilder);
    piecesChart = piecesChartBuilder.getChart();

    ChartBuilder rawBytesChartBuilder = new ChartBuilder("Raw Bytes");
    rawBytesChartBuilder.setValueAxisLabel("B/s");
    addDerivedDatasetIfAvailable("In", "network.rawbytes.in", rawBytesChartBuilder);
    addDerivedDatasetIfAvailable("Out", "network.rawbytes.out", rawBytesChartBuilder);
    rawBytesChart = rawBytesChartBuilder.getChart();

    ChartBuilder trackerChartBuilder = new ChartBuilder("Tracker");
    trackerChartBuilder.setValueAxisLabel("Peers");
    addDatasetIfAvailable("Seeds", "tracker.seeds", trackerChartBuilder);
    addDatasetIfAvailable("Leechers", "tracker.leechers", trackerChartBuilder);
    addDatasetIfAvailable("Distinct Peers", "torrent.distinctpeers", trackerChartBuilder);
    trackerChart = trackerChartBuilder.getChart();

    ChartBuilder connectionChartBuilder = new ChartBuilder("Connections");
    connectionChartBuilder.setValueAxisLabel("Connections");
    addDatasetIfAvailable("In", "network.connections.in", connectionChartBuilder);
    addDatasetIfAvailable("Out", "network.connections.out", connectionChartBuilder);
    connectionChart = connectionChartBuilder.getChart();

    ChartBuilder blockChartBuilder = new ChartBuilder("Blocks");
    blockChartBuilder.setValueAxisLabel("Blocks");
    addDatasetIfAvailable("Leecher", "torrent.blocks.leecher", blockChartBuilder);
    addDatasetIfAvailable("Seed", "torrent.blocks.seed", blockChartBuilder);
    addDatasetIfAvailable("Duplicates", "torrent.blocks.duplicates", blockChartBuilder);
    blockChart = blockChartBuilder.getChart();
  }

  private void addDerivedDatasetIfAvailable(String datasetName, String identifier, ChartBuilder chartBuilder) {
    CSVStream stream = collector.getStream(identifier);
    if (stream != null) {
      chartBuilder.addDerivedDataset(datasetName, stream);
    }
  }

  private void addDatasetIfAvailable(String datasetName, String identifier, ChartBuilder chartBuilder) {
    CSVStream stream = collector.getStream(identifier);
    if (stream != null) {
      chartBuilder.addDataset(datasetName, stream);
    }
  }

  public void create(OutputStream os, File directory) throws IOException {
    buildCharts();
    directory.mkdirs();
    StringBuilder htmlFile = new StringBuilder();
    htmlFile.append("<html>\n");
    htmlFile.append("<h1 style=\"page-break-before: always;\">").append(getName()).append("</h1>\n");
    htmlFile.append("Running Time: ").append(getRunningTime() / 1000 / 60).append("min").append("<br/>\n");
    htmlFile.append("Total Down: ").append(getLastValue("network.rawbytes.in") / 1024 / 1024).append("MB").append("<br/>\n");
    htmlFile.append("Total Up: ").append(getLastValue("network.rawbytes.out") / 1024 / 1024).append("MB").append("<br/>\n");
    File piecesFile = new File(directory, "pieces.png");
    ChartUtilities.saveChartAsPNG(piecesFile, piecesChart, ReportBitThief.WIDTH, ReportBitThief.HEIGHT);
    appendHTMLForGraph(htmlFile, piecesFile, "Pieces", ReportBitThief.WIDTH, ReportBitThief.HEIGHT);
    File rawBytesFile = new File(directory, "rawbytes.png");
    ChartUtilities.saveChartAsPNG(rawBytesFile, rawBytesChart, ReportBitThief.WIDTH, ReportBitThief.HEIGHT);
    appendHTMLForGraph(htmlFile, rawBytesFile, "Raw Bytes", ReportBitThief.WIDTH, ReportBitThief.HEIGHT);
    File trackerFile = new File(directory, "tracker.png");
    ChartUtilities.saveChartAsPNG(trackerFile, trackerChart, ReportBitThief.WIDTH, ReportBitThief.HEIGHT);
    appendHTMLForGraph(htmlFile, trackerFile, "Tracker", ReportBitThief.WIDTH, ReportBitThief.HEIGHT);
    File connectionFile = new File(directory, "connections.png");
    ChartUtilities.saveChartAsPNG(connectionFile, connectionChart, ReportBitThief.WIDTH, ReportBitThief.HEIGHT);
    appendHTMLForGraph(htmlFile, connectionFile, "Connections", ReportBitThief.WIDTH, ReportBitThief.HEIGHT);
    File blockFile = new File(directory, "blocks.png");
    ChartUtilities.saveChartAsPNG(blockFile, blockChart, ReportBitThief.WIDTH, ReportBitThief.HEIGHT);
    appendHTMLForGraph(htmlFile, blockFile, "Blocks", ReportBitThief.WIDTH, ReportBitThief.HEIGHT);

    os.write(htmlFile.toString().getBytes());
  }

  public void create(File directory) throws IOException {
    directory.mkdirs();

    FileOutputStream fos = new FileOutputStream(new File(directory, getName() + ".html"));
    create(fos, directory);
    fos.close();
  }
}
