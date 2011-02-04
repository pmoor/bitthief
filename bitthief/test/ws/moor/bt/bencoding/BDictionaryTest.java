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

/**
 * TODO(pmoor): Javadoc
 */
public class BDictionaryTest extends ExtendedTestCase {

  BDictionary<BEntity> a;

  public void testEquals() {
    BDictionary<BEntity> b = new BDictionary<BEntity>();
    b.put(new BString("hi"), new BString("how are you"));
    b.put(new BString("hello world"), new BInteger(42));

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }

  public void testEncode() throws IOException {
    assertArrayEquals("d11:hello worldi42e2:hi11:how are youe".getBytes(), a.encode());
  }

  public void testOrdering() throws IOException {
    BDictionary<BEntity> dictionary = new BDictionary<BEntity>();
    dictionary.put(new BString("z - end"), new BInteger(5));
    dictionary.put(new BString("a - begin"), new BInteger(10));

    assertEquals("d9:a - begini10e7:z - endi5ee", new String(dictionary.encode()));
  }

  public void setUp() throws Exception {
    super.setUp();
    a = new BDictionary<BEntity>();
    a.put(new BString("hello world"), new BInteger(42));
    a.put(new BString("hi"), new BString("how are you"));
  }
}
