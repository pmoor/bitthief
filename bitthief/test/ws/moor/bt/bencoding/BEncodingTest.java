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

import ws.moor.bt.util.ExtendedTestCase;

import java.io.IOException;

public class BEncodingTest extends ExtendedTestCase {

  public void testIntegerEncoding() throws IOException {
    BInteger integer = new BInteger(42);
    byte[] expected = "i42e".getBytes();

    assertArrayEquals(expected, integer.encode());
  }

  public void testStringEncoding() throws IOException {
    BString string = new BString("Hello World");
    byte[] expected = "11:Hello World".getBytes();

    assertArrayEquals(expected, string.encode());
  }

  public void testListEncoding() throws IOException {
    BList<BEntity> list = new BList<BEntity>();
    list.add(new BInteger(42));
    list.add(new BString("Hello World"));
    byte[] expected = "li42e11:Hello Worlde".getBytes();

    assertArrayEquals(expected, list.encode());
  }

  public void testDictionaryEncoding() throws IOException {
    BDictionary<BInteger> dictionary = new BDictionary<BInteger>();
    dictionary.put(new BString("Hello World"), new BInteger(42));
    dictionary.put(new BString("144"), new BInteger(12));
    String expected = "d3:144i12e11:Hello Worldi42ee";

    assertEquals(expected, new String(dictionary.encode()));
  }

  public void testEncodeAndDecode() throws IOException, ParseException {
    BList<BEntity> list = new BList<BEntity>();
    list.add(new BInteger(42));
    list.add(new BString("Hello World"));
    BDictionary<BEntity> dictionary = new BDictionary<BEntity>();
    dictionary.put(new BString("Hello World"), new BString("What's up?"));
    dictionary.put(new BString("118"), new BInteger(100));
    dictionary.put(new BString("12"), new BString("12"));
    BList<BEntity> list2 = new BList<BEntity>();
    list2.add(new BString("deep down"));
    dictionary.put(new BString(":-12"), list2);
    list.add(dictionary);

    assertEquals(list, new BDecoder().decode(list.encode()));
  }
}
