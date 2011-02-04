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

import java.util.Arrays;

/**
 * TODO(pmoor): Javadoc
 */
public class StringUtilTest extends ExtendedTestCase {

  public void testJoin() {
    assertEquals("a, b, c", StringUtil.join(Arrays.asList("a", "b", "c"), ", "));
    assertEquals("abc", StringUtil.join(Arrays.asList("a", "b", "c"), ""));
    assertEquals("abc", StringUtil.join(Arrays.asList("a", "b", "c"), null));
    assertEquals("A", StringUtil.join(Arrays.asList("A"), null));
    assertEquals("", StringUtil.join(Arrays.asList(), ","));
  }

  public void testRepeat() {
    assertEquals("aaaa", StringUtil.repeat("a", 4));
    assertEquals("aaaa", StringUtil.repeat("aa", 2));
    assertEquals("", StringUtil.repeat("abc", 0));
    assertEquals("abc", StringUtil.repeat("abc", 1));
    assertEquals("abc.abc.abc.", StringUtil.repeat("abc.", 3));
  }
}
