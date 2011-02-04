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

import ws.moor.bt.Environment;
import ws.moor.bt.bencoding.ParseException;
import ws.moor.bt.downloader.TorrentDownload;
import ws.moor.bt.downloader.TorrentDownloadConfiguration;
import ws.moor.bt.torrent.MetaInfo;
import ws.moor.bt.torrent.MetaInfoBuilder;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * TODO(pmoor): Javadoc
 */
public class NewTorrentDownloadWizard {

  private final Environment environment;

  private final JTextField metaFilePathField;
  private final JTextField outputDirectoryPathField;
  private final JTextField shareRatioField;
  private final JTextField listeningPortField;

  private final JFrame frame;

  public NewTorrentDownloadWizard(Environment environment) {
    this.environment = environment;

    JPanel root = new JPanel();
    root.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 3, 2, 3);
    
    JLabel torrentFileLabel = getTorrentFileLabel();
    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.weightx = 0.0;
    root.add(torrentFileLabel, c);

    metaFilePathField = getTorrentFileField();
    c.gridx = 1;
    c.gridy = 0;
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 1.0;
    root.add(metaFilePathField, c);

    JButton torrentFileButton = getTorrentFileButton();
    c.gridx = 2;
    c.gridy = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 0.0;
    root.add(torrentFileButton, c);

    JLabel destinationDirectoryLabel = getDestinationDirectoryLabel();
    c.gridx = 0;
    c.gridy = 1;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.weightx = 0.0;
    root.add(destinationDirectoryLabel, c);

    outputDirectoryPathField = getDestinationDirectoryField();
    c.gridx = 1;
    c.gridy = 1;
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 1.0;
    root.add(outputDirectoryPathField, c);

    JButton destinationDirectoryButton = getDestinationDirectoryButton();
    c.gridx = 2;
    c.gridy = 1;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 0.0;
    root.add(destinationDirectoryButton, c);

    JLabel shareRatioLabel = getShareRatioLabel();
    c.gridx = 0;
    c.gridy = 2;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.weightx = 0.0;
    root.add(shareRatioLabel, c);

    shareRatioField = getShareRatioField();
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 1.0;
    root.add(shareRatioField, c);

    JLabel listeningPortLabel = getListeningPortLabel();
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 1;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_END;
    c.weightx = 0.0;
    root.add(listeningPortLabel, c);

    listeningPortField = getListeningPortField();
    c.gridx = 1;
    c.gridy = 3;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 1.0;
    root.add(listeningPortField, c);

    JButton startButton = new JButton("Start Download");
    startButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        startDownload();
      }
    });

    frame = new JFrame();
    frame.setLayout(new BorderLayout(5, 5));
    frame.add(root, BorderLayout.CENTER);
    frame.add(startButton, BorderLayout.SOUTH);
    frame.pack();
    frame.setResizable(false);
    frame.setVisible(true);
  }

  private JTextField getListeningPortField() {
    return new JTextField(Integer.toString(TorrentDownloadConfiguration.DEFAULT_LISTENING_PORT));
  }

  private JLabel getListeningPortLabel() {
    return new JLabel("Listening Port:");
  }

  private JTextField getShareRatioField() {
    double randomRatio = Math.random() / 3.0 + 1.0;
    randomRatio = Math.round(randomRatio * 100) / 100.0;
    return new JTextField(Double.toString(randomRatio));
  }

  private JLabel getShareRatioLabel() {
    return new JLabel("Sharing Ratio:");
  }

  private JLabel getTorrentFileLabel() {
    return new JLabel("Torrent Metafile:");
  }

  private JTextField getTorrentFileField() {
    return new JTextField(32);
  }

  private JButton getTorrentFileButton() {
    JButton button = new JButton("Search...");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File currentFile = getTorrentFile();
        File file = new TorrentFileChooser(frame).getFile(currentFile);
        if (file != null) {
          metaFilePathField.setText(file.getAbsolutePath());
        }
      }
    });
    return button;
  }

  private File getTorrentFile() {
    if (metaFilePathField.getText().length() > 0) {
      return new File(metaFilePathField.getText());
    }
    return null;
  }

  private JLabel getDestinationDirectoryLabel() {
    return new JLabel("Destination Directory:");
  }

  private JTextField getDestinationDirectoryField() {
    return new JTextField(32);
  }

  private JButton getDestinationDirectoryButton() {
    JButton button = new JButton("Search...");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File currentFile = getOutputDirectory();
        File file = new DestinationDirectoryFileChooser(frame).getFile(currentFile);
        if (file != null) {
          outputDirectoryPathField.setText(file.getAbsolutePath());
        }
      }
    });
    return button;
  }

  private File getOutputDirectory() {
    if (outputDirectoryPathField.getText().length() > 0) {
      return new File(outputDirectoryPathField.getText());
    }
    return null;
  }

  private void startDownload() {
    if (getTorrentFile() == null || !getTorrentFile().canRead()) {
      JOptionPane.showMessageDialog(
          frame, "Cannot read metafile",
          "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    MetaInfo metaInfo = null;
    try {
      metaInfo = MetaInfoBuilder.fromFile(getTorrentFile());
    } catch (IOException e) {
      JOptionPane.showMessageDialog(
          frame, "IO Error during reading of metainfo file: " + e.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
      return;
    } catch (ParseException e) {
      JOptionPane.showMessageDialog(
          frame, "Parse exception during parsing of metainfo file: " + e.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    File outputDirectory = getOutputDirectory();
    if (outputDirectory == null || !outputDirectory.canWrite() || !outputDirectory.isDirectory()) {
      JOptionPane.showMessageDialog(
          frame, "Cannot write to output directory",
          "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    double shareRatio = TorrentDownloadConfiguration.DEFAULT_SHARE_RATIO;
    try {
      shareRatio = Double.parseDouble(shareRatioField.getText());
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(
          frame, "Invalid Share Ratio",
          "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    int listeningPort = TorrentDownloadConfiguration.DEFAULT_LISTENING_PORT;
    try {
      listeningPort = Integer.parseInt(listeningPortField.getText());
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(
          frame, "Invalid Listening Port",
          "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (listeningPort < 1 || listeningPort > 0xffff) {
      JOptionPane.showMessageDialog(
          frame, "Invalid Listening Port",
          "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    final TorrentDownloadConfiguration configuration =
        new TorrentDownloadConfiguration(metaInfo, outputDirectory);
    configuration.setShareRatio(shareRatio);
    configuration.setListeningPort(listeningPort);
    frame.dispose();
    environment.getScheduledExecutor().schedule(new Runnable() {
      public void run() {
        TorrentDownload download = new TorrentDownload(environment, configuration);
        download.start();
      }
    }, 0, TimeUnit.SECONDS);
  }
}
