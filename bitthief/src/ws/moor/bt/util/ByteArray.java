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

package ws.moor.bt.util;

import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;

/**
 * TODO(pmoor): Javadoc
 */
public class ByteArray {

  protected byte[] bytes;

  protected ByteArray() {
    bytes = null;
  }

  protected ByteArray(byte[] bytes) {
    if (bytes == null) {
      throw new NullPointerException();
    }
    this.bytes = bytes;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return Arrays.equals(bytes, ((ByteArray) o).bytes);
  }

  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

  public String toString() {
    return new String(Hex.encodeHex(bytes));
  }

  public String toStringWithRaw() {
    StringBuilder result = new StringBuilder(4 * bytes.length);
    for (byte b : bytes) {
      if (Character.isLetterOrDigit(b)) {
        result.append((char) b);
      } else {
        result.append(".");
      }
    }
    result.append(" (").append(Hex.encodeHex(bytes)).append(")");
    return result.toString();
  }
}
