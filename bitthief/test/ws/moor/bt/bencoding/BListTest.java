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
public class BListTest extends ExtendedTestCase {

  private BList<BString> a;
  private BList<BString> b;

  protected void setUp() throws Exception {
    a = new BList<BString>();
    a.add(new BString("A"));
    a.add(new BString("B"));
    b = new BList<BString>();
    b.add(new BString("A"));
  }

  public void testEquals() {
    BList<BString> c = new BList<BString>();
    c.add(new BString("A"));
    c.add(new BString("B"));
    assertEquals(a, c);
  }

  public void testNotEquals() {
    assertFalse(a.equals(b));
  }

  public void testHashCode() {
    BList<BString> c = new BList<BString>();
    c.add(new BString("A"));
    c.add(new BString("B"));
    assertEquals(a.hashCode(), c.hashCode());
  }

  public void testEncode() throws IOException {
    assertArrayEquals("l1:A1:Be".getBytes(), a.encode());
  }
}
