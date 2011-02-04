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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

/**
 * TODO(pmoor): Javadoc
 */
public class BitField implements Cloneable {

  private BitSet bitSet;
  private int pieceCount;

  public BitField(int pieceCount) {
    this(pieceCount, new BitSet(pieceCount));
  }

  public BitField(int pieceCount, int ... piecesAvailable) {
    this(pieceCount);
    for (int pieceIndex : piecesAvailable) {
      gotPiece(pieceIndex);
    }
  }

  private BitField(int pieceCount, BitSet bitSet) {
    this.pieceCount = pieceCount;
    this.bitSet = bitSet;
  }

  public boolean hasPiece(int pieceIndex) {
    assertValidPieceIndex(pieceIndex);
    return bitSet.get(pieceIndex);
  }

  public void gotPiece(int pieceIndex) {
    assertValidPieceIndex(pieceIndex);
    bitSet.set(pieceIndex);
  }

  public void setRandomPieces(int piecesSet, Random rnd) {
    if (piecesSet > pieceCount) {
      throw new IllegalArgumentException("cannot set more pieces than available");
    } else if (piecesSet < 0) {
      throw new IllegalArgumentException("cannot un-set any pieces");
    }
    List<Integer> pieces = new ArrayList<Integer>();
    for (int i = 0; i < pieceCount; i++) {
      pieces.add(i);
    }

    for (int i = pieceCount - piecesSet; i > 0; i--) {
      pieces.remove(rnd.nextInt(pieces.size()));
    }

    for (int piece : pieces) {
      gotPiece(piece);
    }
  }

  private void assertValidPieceIndex(int pieceIndex) {
    if (pieceIndex < 0 || pieceIndex >= pieceCount) {
      throw new IndexOutOfBoundsException("pieceIndex is out of bounds");
    }
  }

  public byte[] toArray() {
    byte[] result = new byte[getByteCount()];
    for (int i = 0; i < pieceCount; i++) {
      if (bitSet.get(i)) {
        int byteIndex = i / Byte.SIZE;
        int bitIndex = i % Byte.SIZE;
        result[byteIndex] |= 1 << (Byte.SIZE - bitIndex - 1);
      }
    }
    return result;
  }

  public static BitField fromArray(byte[] buffer, int offset, int length) {
    if (length < 0) {
      throw new IllegalArgumentException("length is negative");
    } else if (offset < 0) {
      throw new IllegalArgumentException("offset is negative");
    } else if (buffer.length < offset + length) {
      throw new IllegalArgumentException("buffer is too short");
    }
    int pieceCount = length * Byte.SIZE;
    BitSet bitSet = new BitSet(pieceCount);
    for (int i = 0; i < pieceCount; i++) {
      int byteIndex = offset + i / Byte.SIZE;
      int bitIndex = i % Byte.SIZE;
      if ((buffer[byteIndex] & (1 << (Byte.SIZE - bitIndex - 1))) != 0) {
        bitSet.set(i);
      }
    }
    return new BitField(pieceCount, bitSet);
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(bitSet.cardinality());
    builder.append("/");
    builder.append(pieceCount);
    builder.append(": ");
    builder.append(bitSet.toString());
    return builder.toString();
  }

  public BitField clone() {
    try {
      BitField result = (BitField) super.clone();
      result.bitSet = (BitSet) bitSet.clone();
      return result;
    } catch (CloneNotSupportedException e) {
      // this should never happen
      return null;
    }
  }

  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final BitField bitField = (BitField) o;
    if (pieceCount != bitField.pieceCount) {
      return false;
    }
    return bitSet.equals(bitField.bitSet);
  }

  public int hashCode() {
    int result = bitSet.hashCode();
    result = 29 * result + pieceCount;
    return result;
  }

  public int getByteCount() {
    return (int) Math.ceil((double) pieceCount / Byte.SIZE);
  }

  public BitField downcastTo(int pieceCount) {
    if (pieceCount > this.pieceCount) {
      throw new IllegalArgumentException("cannot extend in downcast");
    } else if (pieceCount <= (getByteCount() - 1) * 8) {
      throw new IllegalArgumentException("cannot downcast by more than one byte (" + pieceCount + " vs. " + 8 * getByteCount());
    }
    return new BitField(pieceCount, (BitSet) bitSet.clone());
  }

  public int getPieceCount() {
    return pieceCount;
  }

  public int getAvailablePieceCount() {
    return bitSet.cardinality();
  }

  public BitField minus(BitField other) {
    if (pieceCount != other.pieceCount) {
      throw new IllegalArgumentException("piece counts do not match");
    }
    BitField result = clone();
    result.bitSet.andNot(other.bitSet);
    return result;
  }

  public List<Integer> availablePieces() {
    List<Integer> result = new ArrayList<Integer>();
    for (int i = 0; i < pieceCount; i++) {
      if (bitSet.get(i)) {
        result.add(i);
      }
    }
    return result;
  }

  public int getMissingPieceCount() {
    return getPieceCount() - getAvailablePieceCount();
  }

  public boolean hasAll() {
    return getPieceCount() == getAvailablePieceCount();
  }
}
