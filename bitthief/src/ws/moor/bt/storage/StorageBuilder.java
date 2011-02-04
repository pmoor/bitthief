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

package ws.moor.bt.storage;

import ws.moor.bt.torrent.MetaInfo;

import java.io.File;

/**
 * TODO(pmoor): Javadoc
 */
public class StorageBuilder {

  private FileRepository fileRepository = null;

  public void setFileRepository(FileRepository fileRepository) {
    this.fileRepository = fileRepository;
  }

  public Storage buildStorage(MetaInfo metaInfo, File parentDirectory) {
    File rootDirectory = metaInfo.determineRootDirectory(parentDirectory);
    Storage result = new Storage(getFileRepository(), rootDirectory,
        metaInfo.getFileInfos(), metaInfo.getPieceLength());
    return result;
  }

  private FileRepository getFileRepository() {
    if (fileRepository == null) {
      synchronized (this) {
        fileRepository = createDefaultFileRepository();
      }
    }
    return fileRepository;
  }

  private FileRepository createDefaultFileRepository() {
    return new NativeFileRepository();
  }
}
