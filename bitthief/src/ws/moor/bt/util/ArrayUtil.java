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

import java.lang.reflect.Array;

/**
 * TODO(pmoor): Javadoc
 */
public class ArrayUtil {
  public static byte[] resize(byte[] array, int newSize) {
    if (newSize < 0) {
      throw new IllegalArgumentException();
    } else if (newSize == array.length) {
      return array;
    }
    byte[] result = new byte[newSize];
    System.arraycopy(array, 0, result, 0, Math.min(array.length, newSize));
    return result;
  }

  public static byte[] cutFront(byte[] array, int cutoff) {
    if (cutoff < 0 || cutoff > array.length) {
      throw new IllegalArgumentException();
    }
    byte[] result = new byte[array.length - cutoff];
    System.arraycopy(array, cutoff, result, 0, result.length);
    return result;
  }

  public static byte[] append(byte[] array, byte[] tail) {
    byte[] result = resize(array, array.length + tail.length);
    System.arraycopy(tail, 0, result, array.length, tail.length);
    return result;
  }

  public static <T> T[] append(T[] array, T tail) {
    T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
    System.arraycopy(array, 0, result, 0, array.length);
    result[array.length] = tail;
    return result;
  }

  public static byte[] subArray(byte[] source, int offset, int length) {
    if (offset == 0 && length == source.length) {
      return source;
    }
    byte[] result = new byte[length];
    System.arraycopy(source, offset, result, 0, length);
    return result;
  }

  public static boolean contains(int needle, int[] haystack) {
    for (int element : haystack) {
      if (element == needle) {
        return true;
      }
    }
    return false;
  }
}
