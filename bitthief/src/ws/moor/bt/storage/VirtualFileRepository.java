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

import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * TODO(pmoor): Javadoc
 */
public class VirtualFileRepository implements FileRepository {

  private Map<File, byte[]> files = new HashMap<File, byte[]>();

  public void write(byte[] bytes, long srcOffset, File file, long dstOffset, long size) {
    byte[] array = files.get(file);
    if (array == null || array.length < dstOffset + size) {
      byte[] array2 = new byte[(int) (dstOffset + size)];
      if (array != null) {
        System.arraycopy(array, 0, array2, 0, array.length);
      }
      array = array2;
      files.put(file, array);
    }
    System.arraycopy(bytes, (int) srcOffset, array, (int) dstOffset, (int) size);
  }

  public void read(File file, long srcOffset, byte[] bytes, long dstOffset, long size) {
    byte[] array = files.get(file);
    if (array == null || array.length < srcOffset + size) {
      byte[] array2 = new byte[(int) (srcOffset + size)];
      if (array != null) {
        System.arraycopy(array, 0, array2, 0, array.length);
      }
      array = array2;
      files.put(file, array);
    }
    System.arraycopy(array, (int) srcOffset, bytes, (int) dstOffset, (int) size);
  }
}
