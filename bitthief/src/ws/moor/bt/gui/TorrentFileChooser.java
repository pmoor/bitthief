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

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.io.File;

/**
 * TODO(pmoor): Javadoc
 */
public class TorrentFileChooser extends JFileChooser {

  private final Component parent;

  public TorrentFileChooser(Component parent) {
    this.parent = parent;
    setFileFilter(new TorrentFileFilter());
    setFileSelectionMode(JFileChooser.FILES_ONLY);
  }

  public File getFile(File currentFile) {
    if (currentFile != null) {
      setSelectedFile(currentFile);
    }
    int result = showOpenDialog(parent);
    if (result != JFileChooser.APPROVE_OPTION) {
      return null;
    }
    return getSelectedFile();
  }

  private static class TorrentFileFilter extends FileFilter {
    public boolean accept(File f) {
      return f.isDirectory() ||
          f.canRead() && f.getName().toLowerCase().endsWith(".torrent");
    }

    public String getDescription() {
      return "Torrent Files";
    }
  }
}
