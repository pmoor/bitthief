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
public class DestinationDirectoryFileChooser extends JFileChooser {

  private final Component parent;

  public DestinationDirectoryFileChooser(Component parent) {
    this.parent = parent;
    setFileFilter(new DestinationDirectoryFileChooser.DirectoryFileFilter());
    setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    setAcceptAllFileFilterUsed(false);
  }

  public File getFile(File currentDirectory) {
    if (currentDirectory != null) {
      setSelectedFile(currentDirectory);
    }
    int result = showSaveDialog(parent);
    if (result != JFileChooser.APPROVE_OPTION) {
      return null;
    }
    return getSelectedFile();
  }

  private static class DirectoryFileFilter extends FileFilter {
    public boolean accept(File f) {
      return f.isDirectory();
    }

    public String getDescription() {
      return "Directories";
    }
  }
}
