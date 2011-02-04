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
import ws.moor.bt.stats.CounterStatistics;
import ws.moor.bt.storage.BitField;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

public class TorrentDownloadDashboard extends JPanel {

  private final TorrentDownload download;

  private final JProgressBar bar;
  private final JButton stopButton;
  private final JButton detailsButton;

  private final DownloadDetails downloadDetails;

  private static final int HORIZONTAL_GAP = 5;

  public TorrentDownloadDashboard(TorrentDownload download) {
    this.download = download;
    setLayout(new BorderLayout(HORIZONTAL_GAP, 0));
    bar = createProgressBar();
    stopButton = createStopButton();
    detailsButton = createDetailsButton();
    downloadDetails = new DownloadDetails(download);

    add(detailsButton, BorderLayout.WEST);
    add(bar, BorderLayout.CENTER);
    add(stopButton, BorderLayout.EAST);
    setBorder(BorderFactory.createTitledBorder(download.getMetaInfo().getName()));
    update();
  }

  private JButton createDetailsButton() {
    JButton button = new JButton("Details...");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        downloadDetails.focus();
      }
    });
    return button;
  }

  private JProgressBar createProgressBar() {
    JProgressBar bar = new JProgressBar();
    bar.setStringPainted(true);
    return bar;
  }

  private JButton createStopButton() {
    JButton button = new JButton("Stop");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        disableButtons();
        download.stop();
        downloadDetails.destroy();
      }
    });
    return button;
  }

  private void disableButtons() {
    stopButton.setEnabled(false);
    detailsButton.setEnabled(false);
  }

  public void update() {
    BitField validPieces = download.getPieceManager().getValidPieces();

    DecimalFormat df = new DecimalFormat();
    double percents = 100d * validPieces.getAvailablePieceCount() / validPieces.getPieceCount();
    df.setMaximumFractionDigits(2);

    bar.setMinimum(0);
    bar.setMaximum(validPieces.getPieceCount());
    bar.setValue(validPieces.getAvailablePieceCount());
    bar.setString(df.format(percents) + "% @ " + df.format(getCurrentSpeed()) + "KB/s");
  }

  private double getCurrentSpeed() {
    CounterStatistics statistics = download.getCounterRepository().getStatistics("network.rawbytes.in");
    return statistics.getAverageIncrease();
  }
}
