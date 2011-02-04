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

import org.apache.log4j.Logger;

import java.io.File;
import java.util.Random;

/**
 * TODO(pmoor): Javadoc
 */
public class TestUtil {

  private static final Logger logger = LoggingUtil.getLogger(TestUtil.class);

  private static final Random rnd = new Random();
  private static File userDirectory = null;

  private static TimeSource timeSource = SystemTimeSource.INSTANCE;

  public static File getTempDir() {
    File directory = new File(getUserTemporaryDirectory(), getRandomFileName());
    directory.mkdirs();
    return directory;
  }

  public static File getTempFile() {
    return new File(getUserTemporaryDirectory(), getRandomFileName());
  }

  private static String getRandomFileName() {
    return Long.toString(Math.abs(rnd.nextInt()), 16);
  }

  private static synchronized File getUserTemporaryDirectory() {
    if (userDirectory == null) {
      String directoryName = System.getProperty("user.name") + "-" + timeSource.getTime();
      userDirectory = new File(getSystemTemporaryDirectory(), directoryName);
      userDirectory.mkdir();
      registerShutdownHook();
    }
    return userDirectory;
  }

  private static File getSystemTemporaryDirectory() {
    return new File(System.getProperty("java.io.tmpdir"));
  }

  private static void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(new MessCleaner()));
  }

  private static class MessCleaner implements Runnable {
    public void run() {
      recurse(userDirectory);
    }

    private void recurse(File directory) {
      if (!directory.isDirectory()) {
        return;
      }
      for (File file : directory.listFiles()) {
        if (file.isDirectory()) {
          recurse(file);
        } else if (file.isFile()) {
          file.delete();
        }
      }
      directory.delete();
    }
  }
}
