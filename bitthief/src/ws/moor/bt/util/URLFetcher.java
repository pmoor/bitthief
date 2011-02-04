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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * TODO(pmoor): Javadoc
 */
public class URLFetcher {

  private final static Logger logger = LoggingUtil.getLogger(URLFetcher.class);

  private final URL url;

  private static final String DEFAULT_USER_AGENT = "BitTorrent/4.4.0";
  private static final int DEFAULT_TIMEOUT = 30 * 1000;

  private URLFetcher(URL url) {
    this.url = url;
  }

  public byte[] fetch() throws IOException {
    InputStream stream = null;
    try {
      stream = getStream();
    } catch (IOException e) {
      logger.warn("exception during fetching of url " + url, e);
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    StreamUtil.copy(stream, baos);
    stream.close();
    return baos.toByteArray();
  }

  private InputStream getStream() throws IOException {
    URLConnection connection = url.openConnection();
    connection.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
    connection.setReadTimeout(DEFAULT_TIMEOUT);
    connection.setRequestProperty("Connection", "close");
    connection.setRequestProperty("Accept", "text/html");
    connection.connect();
    return connection.getInputStream();
  }

  public URL getUrl() {
    return url;
  }

  public static class DefaultFactory implements URLFetcherFactory {
    public URLFetcher createURLFetcher(URL url) {
      return new URLFetcher(url);
    }
  }
}
