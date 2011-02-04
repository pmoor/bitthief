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

package ws.moor.bt.et;

import org.apache.log4j.Logger;
import ws.moor.bt.BitThiefConfiguration;
import ws.moor.bt.Version;
import ws.moor.bt.downloader.TorrentDownload;
import ws.moor.bt.torrent.Hash;
import ws.moor.bt.util.LoggingUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class ET {
  private final boolean enabled;
  private final String postURL;

  private static final String CONTENT_TYPE_PROPERTY_NAME = "Content-Type";
  private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

  private static final Logger logger = LoggingUtil.getLogger(ET.class);

  private static enum Event {
    START, STOP
  }

  public ET(BitThiefConfiguration configuration) {
    enabled = configuration.isETEnabled();
    postURL = configuration.getETPostUrl();
  }

  public void callHomeForStart(TorrentDownload download) {
    call(download, Event.START);
  }

  public void callHomeForStop(TorrentDownload download) {
    call(download, Event.STOP);
  }

  private void call(TorrentDownload download, Event event) {
    if (enabled) {
      try {
        doCall(download, event);
      } catch (Throwable e) {
        logger.debug("exception while calling home", e);
      }
    }
  }

  private void doCall(TorrentDownload download, Event event) throws IOException {
    URL url = new URL(postURL);
    URLConnection urlConnection = url.openConnection();
    urlConnection.setRequestProperty(CONTENT_TYPE_PROPERTY_NAME, CONTENT_TYPE);
    urlConnection.setDoInput(true);
    urlConnection.setDoOutput(true);
    urlConnection.setUseCaches(false);
    OutputStream outputStream = urlConnection.getOutputStream();
    outputStream.write(getPostArgs(download, event));

    InputStream inputStream = urlConnection.getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    reader.readLine();
  }

  private byte[] getPostArgs(TorrentDownload download, Event event) {
    ParameterSet parameters = new ParameterSet();
    parameters.addParameter("infohash", Hash.forByteArray(download.getInfoHash().getBytes()).toString());
    parameters.addParameter("size", download.getMetaInfo().getTotalLength());
    parameters.addParameter("version", Version.getLongVersionString());
    parameters.addParameter("event", event.toString());
    parameters.addParameter("pieces", download.getPieceManager().getValidPieces().getAvailablePieceCount());
    parameters.addParameter("piecesize", download.getMetaInfo().getPieceLength());
    return parameters.toPostString().getBytes();
  }
}
