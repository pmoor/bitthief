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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;

public class BInteger extends Number implements BEntity {

  final static char CODE = 'i';
  final static char END_CODE = 'e';

  private final long value;

  public BInteger(long value) {
    this.value = value;
  }

  public int intValue() {
    return (int) value;
  }

  public long longValue() {
    return value;
  }

  public float floatValue() {
    return value;
  }

  public double doubleValue() {
    return value;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof BInteger)) {
      return false;
    }
    BInteger other = (BInteger) obj;
    return value == other.value;
  }

  public int hashCode() {
    return new Long(value).hashCode();
  }

  public String toString() {
    return Long.toString(value);
  }

  public void encode(OutputStream os) throws IOException {
    os.write(CODE);
    os.write(Long.toString(value).getBytes());
    os.write(END_CODE);
  }

  public byte[] encode() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    encode(baos);
    return baos.toByteArray();
  }

  static BInteger parse(PushbackInputStream stream) throws IOException {
    byte[] buffer = new byte[32];
    int k = 0;
    int read = stream.read();
    byte code = BDecoder.checkValidRead(read);
    while (code != BInteger.END_CODE) {
      buffer[k++] = code;
      read = stream.read();
      code = BDecoder.checkValidRead(read);
    }
    String number = new String(buffer, 0, k);
    return new BInteger(Long.parseLong(number));
  }
}
