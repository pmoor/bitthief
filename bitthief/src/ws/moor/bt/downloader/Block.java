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

package ws.moor.bt.downloader;

/**
 * TODO(pmoor): Javadoc
 */
public class Block implements Comparable {

  private final int length;
  private final int pieceIndex;
  private final int offset;

  public Block(int pieceIndex, int offset, int length) {
    this.pieceIndex = pieceIndex;
    this.offset = offset;
    this.length = length;
  }

  public Block(Block block) {
    pieceIndex = block.getPieceIndex();
    offset = block.getOffset();
    length = block.getLength();
  }

  public int getPieceIndex() {
    return pieceIndex;
  }

  public int getOffset() {
    return offset;
  }

  public int getLength() {
    return length;
  }

  public int getEnd() {
    return getOffset() + getLength();
  }

  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Block that = (Block) o;
    return pieceIndex == that.pieceIndex &&
        offset == that.offset &&
        length == that.length;
  }

  public int hashCode() {
    int result = 29 * length + pieceIndex;
    return 29 * result + offset;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getPieceIndex());
    builder.append(":");
    builder.append(getOffset());
    builder.append("-");
    builder.append(getEnd());
    return builder.toString();
  }

  public int compareTo(Object o) {
    Block other = (Block) o;
    if (getPieceIndex() - other.getPieceIndex() != 0) {
      return getPieceIndex() - other.getPieceIndex();
    } else {
      return getOffset() - other.getOffset();
    }
  }
}
