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

package ws.moor.bt.bencoding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class BDecoder {

  public BEntity decode(String data) throws IOException, ParseException {
    return decode(data.getBytes());
  }

  public BEntity decode(byte[] data) throws IOException, ParseException {
    return parse(new PushbackInputStream(new ByteArrayInputStream(data)));
  }

  public BEntity decode(InputStream stream) throws IOException, ParseException {
    return parse(new PushbackInputStream(stream));
  }

  static BEntity parse(PushbackInputStream stream) throws IOException, ParseException {
    int read = stream.read();
    byte code = checkValidRead(read);
    if (code == BInteger.CODE) {
      return BInteger.parse(stream);
    } else if (code == BList.CODE) {
      return BList.parse(stream);
    } else if (code == BDictionary.CODE) {
      return BDictionary.parse(stream);
    } else if (Character.isDigit(code)) {
      stream.unread(read);
      return BString.parse(stream);
    } else {
      throw new IOException("unknown char: \"" + (char) code + "\" + (" + (int) code + ")");
    }
  }

  static byte checkValidRead(int read) throws IOException {
    if (read == -1) {
      throw new IOException("unexpected end of stream");
    }
    return (byte) read;
  }
}
