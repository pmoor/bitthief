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

import ws.moor.bt.util.ByteArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;

public class BString extends ByteArray implements BEntity {

  public BString(String value) {
    super(value.getBytes());
  }

  public BString(byte[] value) {
    super(value);
  }

  public void encode(OutputStream os) throws IOException {
    os.write(Integer.toString(bytes.length).getBytes());
    os.write(':');
    os.write(bytes);
  }

  public byte[] encode() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    encode(baos);
    return baos.toByteArray();
  }

  public String toString() {
    return new String(bytes);
  }

  static BString parse(PushbackInputStream stream) throws IOException, ParseException {
    byte[] buffer = new byte[32];
    int k = 0;
    int read = stream.read();
    byte code = BDecoder.checkValidRead(read);
    while (code != ':') {
      buffer[k++] = code;
      read = stream.read();
      code = BDecoder.checkValidRead(read);
    }
    String number = new String(buffer, 0, k);

    int stringLength = 0;
    try {
      stringLength = Integer.parseInt(number);
    } catch (NumberFormatException e) {
      throw new ParseException("could not parse number: " + number);
    }
    byte[] buffer2 = new byte[stringLength];
    for (int i = 0; i < stringLength; i++) {
      read = stream.read();
      buffer2[i] = BDecoder.checkValidRead(read);
    }

    return new BString(buffer2);
  }
}
