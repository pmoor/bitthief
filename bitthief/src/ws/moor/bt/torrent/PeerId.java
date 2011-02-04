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

import org.apache.commons.codec.binary.Hex;
import ws.moor.bt.util.ByteArray;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.StringUtil;

/**
 * TODO(pmoor): Javadoc
 */
public class PeerId extends ByteArray {

  public static final int LENGTH = 20;

  public PeerId(byte[] id) {
    assertCorrectId(id);
    bytes = id.clone();
  }

  private void assertCorrectId(byte[] id) {
    if (id == null) {
      throw new NullPointerException();
    } else if (id.length != LENGTH) {
      throw new IllegalArgumentException("id should be of length 20");
    }
  }

  public static PeerId createRandomMainlineId() {
    StringBuilder id = new StringBuilder(LENGTH);
    id.append('M');
    id.append("4-4-0");
    id.append(StringUtil.repeat("-", 8 - id.length()));
    id.append(constructRandomHexString(LENGTH - id.length()));
    return new PeerId(id.toString().getBytes());
  }

  public static PeerId createRandom() {
    return new PeerId(ByteUtil.randomByteArray(LENGTH));
  }

  private static String constructRandomHexString(int length) {
    byte[] dataNeeded = ByteUtil.randomByteArray(length * 2 + 1);
    return new String(Hex.encodeHex(dataNeeded), 0, length);
  }

  public byte[] getBytes() {
    return bytes.clone();
  }

  public int distance(PeerId other) {
    return ByteUtil.distance(bytes, other.bytes);
  }

  public PeerId flipLastBit() {
    byte[] newbytes = bytes.clone();
    newbytes[newbytes.length - 1] ^= 1;
    return new PeerId(newbytes);
  }
}
