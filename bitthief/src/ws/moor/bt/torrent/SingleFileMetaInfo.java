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

import java.net.URL;
import java.util.Date;

/**
 * TODO(pmoor): Javadoc
 */
public class SingleFileMetaInfo extends MetaInfo {

  private final long length;

  public SingleFileMetaInfo(Hash infoHash,
                            URL announceUrl,
                            String comment,
                            Date creationDate,
                            int pieceLength,
                            String name,
                            Hash[] pieceHashes,
                            long length) {
    super(infoHash, announceUrl, comment, creationDate, pieceLength, name, pieceHashes);
    this.length = length;
    checkPieceCountConsistency();
  }

  public FileInfo[] getFileInfos() {
    return new MetaInfo.FileInfo[] { new MetaInfo.FileInfo(new String[] { name }, length) };
  }

  public long getTotalLength() {
    return length;
  }

  public boolean equals(Object other) {
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    if (!super.equals(other)) {
      return false;
    }
    return length == ((SingleFileMetaInfo) other).length;
  }
}
