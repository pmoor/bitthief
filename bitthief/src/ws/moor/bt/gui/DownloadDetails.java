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

package ws.moor.bt.gui;

import ws.moor.bt.downloader.TorrentDownload;
import ws.moor.bt.gui.charts.BlockDownload;
import ws.moor.bt.gui.charts.BlockOrigin;
import ws.moor.bt.gui.charts.ConnectionTypes;
import ws.moor.bt.gui.charts.DownloadRatePerPeer;
import ws.moor.bt.gui.charts.Maintainable;
import ws.moor.bt.gui.charts.OpenConnections;
import ws.moor.bt.gui.charts.PiecesStats;
import ws.moor.bt.gui.charts.RawDownloadRate;
import ws.moor.bt.gui.charts.RemotePeerCompletion;
import ws.moor.bt.gui.charts.TotalBlocksPerPeer;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TODO(pmoor): Javadoc
 */
public class DownloadDetails {

  private final TorrentDownload download;
  private final JFrame window;

  private boolean quit = false;
  private List<Maintainable> maintainables = new ArrayList<Maintainable>();

  private static final int MAINTENANCE_PERIOD = 15;
  private Runnable maintainer;

  public DownloadDetails(TorrentDownload download) {
    this.download = download;
    window = createWindow();

    maintainer = new Maintainer();
    download.getEnvironment().getScheduledExecutor().scheduleWithFixedDelay(
        maintainer, MAINTENANCE_PERIOD, MAINTENANCE_PERIOD, TimeUnit.SECONDS);
  }

  private JFrame createWindow() {
    JFrame window = new JFrame("BitThief - " + download.getMetaInfo().getName());
    window.setSize(640, 480);
    window.setLayout(new BorderLayout());

    JTabbedPane tabs = new JTabbedPane();

    addComponent(tabs, "Download Rate", new RawDownloadRate(download.getCounterRepository()));
    addComponent(tabs, "Download Rates Per Peer", new DownloadRatePerPeer(download.getCounterRepository()));
    addComponent(tabs, "Blocks", new BlockDownload(download.getCounterRepository()));
    addComponent(tabs, "Blocks Per Peer", new TotalBlocksPerPeer(download.getCounterRepository()));
    addComponent(tabs, "Block Origin", new BlockOrigin(download.getCounterRepository()));
    addComponent(tabs, "Pieces", new PiecesStats(download.getCounterRepository()));
    addComponent(tabs, "Open Connections", new OpenConnections(download.getCounterRepository()));
    addComponent(tabs, "Connection Types", new ConnectionTypes(download.getCounterRepository()));
    addComponent(tabs, "Peer Completion", new RemotePeerCompletion(download));

    window.add(tabs, BorderLayout.CENTER);

    return window;
  }

  private void addComponent(JTabbedPane tabs, String title, JComponent component) {
    tabs.add(title, component);
    if (component instanceof Maintainable) {
      maintainables.add((Maintainable) component);
    }
  }

  public void focus() {
    maintainer.run();
    if (window.isVisible()) {
      window.requestFocus();
      window.toFront();
    } else {
      window.setVisible(true);
    }
  }

  public void destroy() {
    window.dispose();
    quit = true;
  }

  private class Maintainer implements Runnable {
    public synchronized void run() {
      if (window.isVisible()) {
        for (Maintainable maintainable : maintainables) {
          maintainable.maintain();
        }
      }
      if (quit) {
        throw new RuntimeException("quitting");
      }
    }
  }
}
