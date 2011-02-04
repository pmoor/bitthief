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

package ws.moor.bt.dht;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import ws.moor.bt.BitThiefConfiguration;
import ws.moor.bt.Environment;
import ws.moor.bt.util.LoggingUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * TODO(pmoor): Javadoc
 */
public class StandaloneTracker {

  private Environment environment;
  private DHTracker tracker;

  private static final File STATE_FILE = new File("/tmp/TrackerState");

  private static final Logger logger = LoggingUtil.getLogger(StandaloneTracker.class);

  private TrackerState getTrackerState() throws IOException {
    if (STATE_FILE.canRead()) {
      return TrackerState.createForFile(STATE_FILE);
    }
    return TrackerState.createNew(8031);
  }

  private class TrackerStateSaver implements Runnable {
    public void run() {
      try {
        logger.info("saving tracker state");
        TrackerState state = TrackerState.createForTracker(tracker);
        state.saveToFile(STATE_FILE);
        logger.info("saving done");
      } catch (IOException e) {
        logger.error("exception during state file saving", e);
      }
    }
  }

  private static void configureStaticStuff(BitThiefConfiguration configuration) throws IOException {
    InputStream stream = ClassLoader.getSystemResourceAsStream(configuration.getLoggingPropertyFile());
    if (stream != null) {
      Properties properties = new Properties();
      properties.load(stream);
      PropertyConfigurator.configure(properties);
    } else {
      System.err.println("unable to load logging property file");
      System.exit(1);
    }
  }

  public void run(String[] args) throws Exception {
    BitThiefConfiguration config = BitThiefConfiguration.fromPropertiesFile();
    configureStaticStuff(config);

    environment = new Environment(config);
    tracker = new DHTracker(environment, getTrackerState());
    environment.getScheduledExecutor().scheduleWithFixedDelay(new TrackerStateSaver(), 300, 600, TimeUnit.SECONDS);
  }

  public static void main(String[] args) throws Exception {
    new StandaloneTracker().run(args);
  }
}
