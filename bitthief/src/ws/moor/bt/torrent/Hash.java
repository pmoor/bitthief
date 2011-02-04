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

import ws.moor.bt.util.ByteArray;
import ws.moor.bt.util.DigestUtil;

/**
 * TODO(pmoor): Javadoc
 */
public class Hash extends ByteArray {

  public static final int LENGTH = 20;

  public Hash(byte[] hash) {
    if (hash == null || hash.length != LENGTH) {
      throw new IllegalArgumentException("not a valid hash");
    }
    bytes = new byte[LENGTH];
    System.arraycopy(hash, 0, bytes, 0, LENGTH);
  }

  private Hash(byte[] hash, int offset) {
    if (offset < 0) {
      throw new IllegalArgumentException("invalid offset");
    } else if (hash == null || hash.length < offset + LENGTH) {
      throw new IllegalArgumentException("not a valid hash");
    }
    bytes = new byte[LENGTH];
    System.arraycopy(hash, offset, bytes, 0, LENGTH);
  }

  public boolean equalsHashOf(byte[] data, int offset, int length) {
    return equals(forByteArray(data, offset, length));
  }

  public static Hash forByteArray(byte[] bytes) {
    return forByteArray(bytes, 0, bytes.length);
  }

  public static Hash forByteArray(byte[] bytes, int offset, int length) {
    return new Hash(DigestUtil.sha1(bytes, offset, length));
  }

  public byte[] getBytes() {
    return bytes.clone();
  }

  public static Hash[] fromConcatenatedByteArrays(byte[] arrays) {
    if (arrays == null) {
      throw new NullPointerException();
    }
    if (arrays.length % LENGTH != 0) {
      throw new IllegalArgumentException("array length is not a multiple of " + LENGTH);
    }
    int pieceCount = arrays.length / LENGTH;
    Hash[] pieceHashes = new Hash[pieceCount];
    for (int i = 0; i < pieceCount; i++) {
      pieceHashes[i] = new Hash(arrays, LENGTH * i);
    }
    return pieceHashes;
  }
}
