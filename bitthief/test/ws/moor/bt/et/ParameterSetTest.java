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

import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class ParameterSetTest extends ExtendedTestCase {

  public void testBasics() {
    ParameterSet set = new ParameterSet();
    set.addParameter("A", "1");
    set.addParameter("B", 2);

    String string = set.toPostString();
    assertTrue(string.contains("A=1"));
    assertTrue(string.contains("B=2"));
    assertTrue(string.contains("&"));
  }

  public void testSpecialCharInValue() {
    ParameterSet set = new ParameterSet();
    set.addParameter("A", " äöü ");
    assertEquals("A=+%C3%A4%C3%B6%C3%BC+", set.toPostString());
  }

  public void testSpecialChartInName() {
    ParameterSet set = new ParameterSet();
    set.addParameter("äü", "test");
    assertEquals("%C3%A4%C3%BC=test", set.toPostString());
  }
}
