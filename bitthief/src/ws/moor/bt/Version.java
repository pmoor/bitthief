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
public class Version {

  private static int major;
  private static int minor;
  private static int tiny;

  private static String revision;

  private static String shortString;
  private static String longString;

  static {
    loadProperties();
  }

  private static void loadProperties() {
    InputStream stream =
        ClassLoader.getSystemResourceAsStream("version.properties");
    if (stream != null) {
      Properties versionProperties = new Properties();
      try {
        versionProperties.load(stream);
        extractVersions(versionProperties);
      } catch (IOException e) {}
    }

    stream =
        ClassLoader.getSystemResourceAsStream("revision.properties");
    if (stream != null) {
      Properties revisionProperties = new Properties();
      try {
        revisionProperties.load(stream);
        extractRevision(revisionProperties);
      } catch (IOException e) {}
    }

    constructVersionStrings();
  }

  private static void constructVersionStrings() {
    StringBuilder builder = new StringBuilder();
    builder.append(major);
    builder.append(".");
    builder.append(minor);
    builder.append(".");
    builder.append(tiny);
    shortString = builder.toString();
    
    if (revision != null) {
      builder.append("-");
      builder.append(revision);
    }
    longString = builder.toString();
  }

  private static void extractRevision(Properties properties) {
    revision = properties.getProperty("version.revision").trim();
  }

  private static void extractVersions(Properties properties) {
    major = Integer.parseInt(properties.getProperty("version.major"));
    minor = Integer.parseInt(properties.getProperty("version.minor"));
    tiny = Integer.parseInt(properties.getProperty("version.tiny"));
  }

  public static int getMajor() {
    return major;
  }

  public static int getMinor() {
    return minor;
  }

  public static int getTiny() {
    return tiny;
  }

  public static String getRevision() {
    return revision;
  }

  public static String getShortVersionString() {
    return shortString;
  }

  public static String getLongVersionString() {
    return longString;
  }
}
