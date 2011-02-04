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

package ws.moor.bt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * TODO(pmoor): Javadoc
 */
public class BitThiefConfiguration {

  private Properties properties;

  private static final String DEFAULT_PROPERTY_FILE = "bitthief.properties";
  private static final String SYSTEM_PROPERTY_KEY = "ws.moor.bt.mainproperty";

  public static BitThiefConfiguration fromPropertiesFile() {
    try {
      return new BitThiefConfiguration(getPropertyFileName());
    } catch (IOException e) {
      return null;
    }
  }

  private static String getPropertyFileName() {
    String systemProperty = System.getProperty(SYSTEM_PROPERTY_KEY);
    if (systemProperty != null) {
      return systemProperty;
    } else {
      return DEFAULT_PROPERTY_FILE;
    }
  }

  private BitThiefConfiguration(String name) throws IOException {
    InputStream stream = ClassLoader.getSystemResourceAsStream(name);
    if (stream != null) {
      properties = new Properties();
      properties.load(stream);
    } else {
      throw new IOException("stream is not valid");
    }
  }

  public boolean doLogStats() {
    return readBooleanProperty("stats.enable");
  }

  public String getStatsLogFile() {
    return properties.getProperty("stats.file");
  }

  public boolean isETEnabled() {
    return readBooleanProperty("et.enable");
  }

  public String getETPostUrl() {
    return properties.getProperty("et.url");
  }

  private boolean readBooleanProperty(String key) {
    return Boolean.parseBoolean(properties.getProperty(key));
  }

  public String getLoggingPropertyFile() {
    return properties.getProperty("logging.config");
  }
}
