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

import org.apache.log4j.Logger;
import ws.moor.bt.util.LoggingUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * TODO(pmoor): Javadoc
 */
public class NativeFileRepository implements FileRepository {

  private static final Logger logger =
      LoggingUtil.getLogger(NativeFileRepository.class);

  public synchronized void write(byte[] bytes, long srcOffset, File file, long dstOffset, long size) {
    createDirectoryIfNeeded(file.getParentFile());
    try {
      RandomAccessFile rndFile = new RandomAccessFile(file, "rw");
      reserveLength(rndFile, dstOffset + size);
      rndFile.seek(dstOffset);
      rndFile.write(bytes, (int) srcOffset, (int) size);
      rndFile.close();
    } catch (IOException e) {
      logger.warn("exception during write", e);
    }
  }

  public synchronized void read(File file, long srcOffset, byte[] bytes, long dstOffset, long size) {
    createDirectoryIfNeeded(file.getParentFile());
    try {
      RandomAccessFile rndFile = new RandomAccessFile(file, "rw");
      reserveLength(rndFile, srcOffset + size);
      rndFile.seek(srcOffset);
      rndFile.read(bytes, (int) dstOffset, (int) size);
      rndFile.close();
    } catch (IOException e) {
      logger.warn("exception during read", e);
    }
  }

  private void createDirectoryIfNeeded(File directory) {
    if (directory.isDirectory()) {
      return;
    }
    directory.mkdirs();
  }

  private void reserveLength(RandomAccessFile rndFile, long length) throws IOException {
    if (rndFile.length() < length) {
      rndFile.setLength(length);
    }
  }
}
