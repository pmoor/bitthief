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

import org.jfree.ui.tabbedui.VerticalLayout;
import ws.moor.bt.Environment;
import ws.moor.bt.Version;
import ws.moor.bt.downloader.TorrentDownload;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class BitThiefGUI {

  private final JFrame window;
  private final Environment environment;
  private final JPanel boardsContainer;
  private final Map<TorrentDownload, TorrentDownloadDashboard> boards =
      new HashMap<TorrentDownload, TorrentDownloadDashboard>();


  public BitThiefGUI(Environment environment) {
    this.environment = environment;

    window = createWindow();
    window.setLayout(new BorderLayout());
    boardsContainer = new JPanel(new VerticalLayout());
    window.add(boardsContainer, BorderLayout.CENTER);
  }

  private void periodicUpdate() {
    Set<TorrentDownloadDashboard> handled = new HashSet<TorrentDownloadDashboard>();
    for (TorrentDownload download : environment.getDownloadRepository().getAllDownloads()) {
      TorrentDownloadDashboard dashboard = boards.get(download);
      if (dashboard != null) {
        dashboard.update();
      } else {
        dashboard = new TorrentDownloadDashboard(download);
        boards.put(download, dashboard);
        boardsContainer.add(dashboard);
        boardsContainer.revalidate();
        boardsContainer.repaint();
      }
      handled.add(dashboard);
    }
    Set<TorrentDownloadDashboard> inactive = new HashSet<TorrentDownloadDashboard>(boards.values());
    inactive.removeAll(handled);
    for (TorrentDownloadDashboard dashboard : inactive) {
      boardsContainer.remove(dashboard);
      boardsContainer.revalidate();
      boardsContainer.repaint();
    }
  }

  private JFrame createWindow() {
    JFrame window = new JFrame("BitThief (" + Version.getLongVersionString() + ")");
    window.setJMenuBar(createMenuBar());
    window.setSize(640, 320);
    window.setLayout(new BorderLayout());

    return window;
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.add(getNewDownloadItem());
    fileMenu.add(new JSeparator());
    fileMenu.add(getQuitItem());
    menuBar.add(fileMenu);
    return menuBar;
  }

  private JMenuItem getNewDownloadItem() {
    JMenuItem newDownloadItem = new JMenuItem("New Download...", 'n');
    newDownloadItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new NewTorrentDownloadWizard(environment);
      }
    });
    return newDownloadItem;
  }

  private JMenuItem getQuitItem() {
    JMenuItem quitItem = new JMenuItem("Quit", 'q');
    quitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        for (TorrentDownload download : environment.getDownloadRepository().getAllDownloads()) {
          try {
            download.stop();
          } catch (Exception e) {
            // ignore for now
          }
        }
        System.exit(0);
      }
    });
    return quitItem;
  }

  public void show() {
    periodicUpdate();
    window.setVisible(true);
    environment.getScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
      public void run() {
        periodicUpdate();
      }
    }, 5, 5, TimeUnit.SECONDS);
  }
}
