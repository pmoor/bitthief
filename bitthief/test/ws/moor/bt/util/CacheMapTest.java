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

import java.util.Map;

/**
 * TODO(pmoor): Javadoc
 */
public class CacheMapTest extends ExtendedTestCase {

  public void testSticksToLimit() {
    Map<String, String> cache = new CacheMap<String, String>(4);

    cache.put("1", "a");
    cache.put("2", "b");
    cache.put("3", "c");
    cache.put("4", "d");

    assertEquals("a", cache.get("1"));
    assertEquals("b", cache.get("2"));
    assertEquals("c", cache.get("3"));
    assertEquals("d", cache.get("4"));

    cache.put("5", "e");

    assertEquals("e", cache.get("5"));
    assertNull(cache.get("1"));
  }

  public void testAccessControlFalse() {
    Map<String, String> cache =
        new CacheMap<String, String>(2, false);

    cache.put("1", "a");
    cache.put("2", "b");
    cache.put("1", "c");
    cache.put("3", "d");

    assertEquals("b", cache.get("2"));
    assertEquals("d", cache.get("3"));
    assertNull(cache.get("1"));
  }

  public void testAccessControlTrue() {
    Map<String, String> cache =
        new CacheMap<String, String>(2, true);

    cache.put("1", "a");
    cache.put("2", "b");
    cache.put("1", "c");
    cache.put("3", "d");

    assertEquals("c", cache.get("1"));
    assertEquals("d", cache.get("3"));
    assertNull(cache.get("2"));
  }

  public void testAccessControlTrueGettingOnly() {
    Map<String, String> cache =
        new CacheMap<String, String>(2, true);

    cache.put("1", "a");
    cache.put("2", "b");
    cache.get("1");
    cache.put("3", "d");

    assertEquals("a", cache.get("1"));
    assertEquals("d", cache.get("3"));
    assertNull(cache.get("2"));
  }
}
