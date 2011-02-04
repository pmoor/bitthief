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

package ws.moor.bt.torrent;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

/**
 * TODO(pmoor): Javadoc
 */
public class MultiFileMetaInfo extends MetaInfo {

  private final FileInfo[] fileInfos;

  private final long totalLength;

  public MultiFileMetaInfo(Hash infoHash,
                           URL announceUrl,
                           String comment,
                           Date creationDate,
                           int pieceLength,
                           String name,
                           Hash[] pieceHashes,
                           MetaInfo.FileInfo[] fileInfos) {
    super(infoHash, announceUrl, comment, creationDate, pieceLength, name, pieceHashes);
    this.fileInfos = fileInfos;
    totalLength = computeTotalLength(fileInfos);
    checkPieceCountConsistency();
  }

  private long computeTotalLength(FileInfo[] infos) {
    long sum = 0;
    for (FileInfo info : infos) {
      sum += info.getLength();
    }
    return sum;
  }

  public FileInfo[] getFileInfos() {
    return fileInfos;
  }

  public long getTotalLength() {
    return totalLength;
  }

  public boolean equals(Object other) {
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    if (!super.equals(other)) {
      return false;
    }
    return Arrays.equals(fileInfos, ((MultiFileMetaInfo) other).fileInfos);
  }

  public File determineRootDirectory(File parentDirectory) {
    return new File(parentDirectory, getName());
  }
}
