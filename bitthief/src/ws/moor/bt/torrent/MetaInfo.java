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

import ws.moor.bt.util.StringUtil;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

/**
 * TODO(pmoor): Javadoc
 */
public abstract class MetaInfo {

  protected final Hash infoHash;
  protected final URL announceUrl;
  protected final String comment;
  protected final Date creationDate;
  protected final int pieceLength;
  protected final String name;
  protected final Hash[] pieceHashes;

  public MetaInfo(Hash infoHash,
                  URL announceUrl,
                  String comment,
                  Date creationDate,
                  int pieceLength,
                  String name,
                  Hash[] pieceHashes) {
    this.infoHash = infoHash;
    this.announceUrl = announceUrl;
    this.comment = comment;
    this.creationDate = creationDate;
    this.pieceLength = pieceLength;
    this.name = name;
    this.pieceHashes = pieceHashes;
  }

  public Hash getInfoHash() {
    return infoHash;
  }

  public URL getAnnounceUrl() {
    return announceUrl;
  }

  public String getComment() {
    return comment;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public int getPieceLength() {
    return pieceLength;
  }

  public String getName() {
    return name;
  }

  public Hash[] getPieceHashes() {
    return pieceHashes;
  }

  public int getPieceCount() {
    return (int) Math.ceil((double) getTotalLength() / pieceLength);
  }

  public abstract FileInfo[] getFileInfos();

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof MetaInfo)) {
      return false;
    }
    MetaInfo other = (MetaInfo) obj;
    if (!infoHash.equals(other.infoHash) &&
        announceUrl.equals(other.announceUrl) &&
        creationDate.equals(other.creationDate) &&
        pieceLength == other.pieceLength &&
        name.equals(other.name) &&
        Arrays.equals(pieceHashes, other.pieceHashes)) {
      return false;
    }

    if (comment == null) {
      return other.comment == null;
    }
    return comment.equals(other.comment);
  }

  public int hashCode() {
    return infoHash.hashCode();
  }

  public abstract long getTotalLength();

  protected void checkPieceCountConsistency() {
    if (pieceHashes.length != getPieceCount()) {
      throw new IllegalArgumentException("piece count and number of hashes do not match");
    }
  }

  public File determineRootDirectory(File parentDirectory) {
    return parentDirectory;
  }

  public static class FileInfo {
    private final String[] path;
    private final long length;

    public FileInfo(String[] path, long length) {
      if (path == null) {
        throw new NullPointerException();
      }
      if (length < 0) {
        throw new IllegalArgumentException("length should be >= 0");
      }
      this.path = path.clone();
      this.length = length;
    }

    public String[] getPath() {
      return path;
    }

    public long getLength() {
      return length;
    }

    public boolean equals(Object that) {
      if (that == null || !(that instanceof FileInfo)) {
        return false;
      }
      return Arrays.equals(path, ((FileInfo) that).path) &&
          length == ((FileInfo) that).length;
    }

    public File constructFile(File parent) {
      String subpath = StringUtil.join(Arrays.asList(path), File.separator);
      return new File(parent, subpath);
    }
  }
}
