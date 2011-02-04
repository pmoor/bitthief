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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.BitSet;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * TODO(pmoor): Javadoc
 */
public class ByteUtil {

  private static Random rnd = new Random();

  public static void int_to_b32(int i, byte[] bytes) {
    int_to_b32(i, bytes, 0);
  }

  public static void int_to_b32(int i, byte[] bytes, int offset) {
    bytes[offset + 0] = (byte) ((i >> 24) & 0xff);
    bytes[offset + 1] = (byte) ((i >> 16) & 0xff);
    bytes[offset + 2] = (byte) ((i >>  8) & 0xff);
    bytes[offset + 3] = (byte) ((i >>  0) & 0xff);
  }

  public static void int_to_l32(int i, byte[] bytes) {
    bytes[3] = (byte) ((i >> 24) & 0xff);
    bytes[2] = (byte) ((i >> 16) & 0xff);
    bytes[1] = (byte) ((i >>  8) & 0xff);
    bytes[0] = (byte) ((i >>  0) & 0xff);
  }

  public static byte[] newByteArray(int ... bytes) {
    byte[] result = new byte[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      if (bytes[i] > 255 || bytes[i] < 0) {
        throw new IllegalArgumentException("all values must be 0 <= value < 256");
      }
      result[i] = (byte) bytes[i];
    }
    return result;
  }

  public static int b32_to_int(byte[] bytes, int offset) {
    return ((bytes[offset + 0] & 0xff) << 24) |
           ((bytes[offset + 1] & 0xff) << 16) |
           ((bytes[offset + 2] & 0xff) <<  8) |
           ((bytes[offset + 3] & 0xff) <<  0);
  }

  public static byte[] randomByteArray(int length) {
    byte[] result = new byte[length];
    rnd.nextBytes(result);
    return result;
  }

  public static int distance(byte[] a, byte[] b) {
    byte[] result = xor(a, b);
    if (result.length == 0) {
      result = new byte[] {1};
    }
    result = ArrayUtil.append(new byte[] {0}, result);
    BigDecimal number = new BigDecimal(new BigInteger(result));
    if (number.equals(BigDecimal.ZERO)) {
      number = BigDecimal.ONE;
    }
    double clear = BigDecimal.valueOf(2).pow(a.length * 8).divide(number, 4, RoundingMode.UP).doubleValue();
    return (int) (100.0 * Math.log(clear) / Math.log(2.0) / a.length);
  }

  public static BitSet toBitSet(byte[] bytes) {
    BitSet result = new BitSet();
    for (int i = 0; i < bytes.length * 8; i++) {
      if ((bytes[i / 8] & (1 << (7 - (i % 8)))) != 0) {
        result.set(i);
      }
    }
    return result;
  }

  public static byte[] xor(byte[] a, byte[] b) {
    if (a == null || b == null) {
      throw new NullPointerException("a and b must not be null");
    } else if (a.length != b.length) {
      throw new IllegalArgumentException("a and b must have the same length");
    }
    byte[] result = new byte[a.length];
    for (int i = 0; i < a.length; i++) {
      result[i] = (byte) (a[i] ^ b[i]);
    }
    return result;
  }

  public static int leftmostBit(byte[] bytes) {
    int bit = 0;
    while (bit / 8 < bytes.length && bytes[bit / 8] == 0) {
      bit += 8;
    }
    if (bit/8 == bytes.length) {
      return -1;
    }
    byte byt = bytes[bit/8];
    for (int i = 7; i >= 0; i--, bit++) {
      if ((byt & (1 << i)) != 0) {
        break;
      }
    }
    return bytes.length * 8 - bit - 1;
  }

  private static char[] urlEncodeMap = new char[256];
  static {
    for (char c = 'a'; c <= 'z'; c++) {
      urlEncodeMap[c] = c;
    }
    for (char c = 'A'; c <= 'Z'; c++) {
      urlEncodeMap[c] = c;
    }
    for (char c = '0'; c <= '9'; c++) {
      urlEncodeMap[c] = c;
    }
    urlEncodeMap['-'] = '-';
  }

  public static String urlEncode(byte[] array) {
    StringBuilder result = new StringBuilder(array.length * 3);
    for (byte byt : array) {
      int intval = byt & 0xff;
      if (urlEncodeMap[intval] > 0) {
        result.append(urlEncodeMap[intval]);
      } else {
        result.append('%');
        result.append(Hex.encodeHex(new byte[] {byt}));
      }
    }
    return result.toString();
  }
}
