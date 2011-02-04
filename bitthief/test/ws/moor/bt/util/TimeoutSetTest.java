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

public class TimeoutSetTest extends ExtendedTestCase {

  private AdjustableTimeSource adjustableTimeSource;

  protected void setUp() throws Exception {
    super.setUp();

    adjustableTimeSource = new AdjustableTimeSource();
  }

  public void testSimpleScenario() {
    TimeoutSet<String> set = new TimeoutSet<String>(4, 10);
    set.setTimeSource(adjustableTimeSource);

    set.add("A");
    set.add("B");
    set.add("C");
    set.add("D");

    assertTrue(set.contains("A"));
    assertTrue(set.contains("B"));
    assertTrue(set.contains("C"));
    assertTrue(set.contains("D"));

    set.add("E");
    assertFalse(set.contains("A"));
    assertTrue(set.contains("B"));
    assertTrue(set.contains("C"));
    assertTrue(set.contains("D"));
    assertTrue(set.contains("E"));

    adjustableTimeSource.increaseTime(5);
    set.add("F");
    assertFalse(set.contains("A"));
    assertFalse(set.contains("B"));
    assertTrue(set.contains("C"));
    assertTrue(set.contains("D"));
    assertTrue(set.contains("E"));
    assertTrue(set.contains("F"));

    adjustableTimeSource.increaseTime(5);
    assertFalse(set.contains("A"));
    assertFalse(set.contains("B"));
    assertFalse(set.contains("C"));
    assertFalse(set.contains("D"));
    assertFalse(set.contains("E"));
    assertTrue(set.contains("F"));
  }

  public void testRemove() {
    TimeoutSet<String> set = new TimeoutSet<String>(4, 10);
    set.setTimeSource(adjustableTimeSource);

    set.add("A");
    set.add("B");
    assertTrue(set.contains("A"));
    assertTrue(set.contains("B"));

    set.remove("A");
    assertFalse(set.contains("A"));
    assertTrue(set.contains("B"));

    set.remove("B");
    assertFalse(set.contains("A"));
    assertFalse(set.contains("B"));
  }

  public void testAddWithCustomTimeout() {
    TimeoutSet<String> set = new TimeoutSet<String>(4, 10);
    set.setTimeSource(adjustableTimeSource);

    set.add("A", 5);
    set.add("B", 7);
    set.add("C");
    assertTrue(set.contains("A"));
    assertTrue(set.contains("B"));
    assertTrue(set.contains("C"));

    adjustableTimeSource.increaseTime(4);
    assertTrue(set.contains("A"));
    assertTrue(set.contains("B"));
    assertTrue(set.contains("C"));

    adjustableTimeSource.increaseTime(1);
    assertFalse(set.contains("A"));
    assertTrue(set.contains("B"));
    assertTrue(set.contains("C"));

    adjustableTimeSource.increaseTime(2);
    assertFalse(set.contains("A"));
    assertFalse(set.contains("B"));
    assertTrue(set.contains("C"));

    adjustableTimeSource.increaseTime(3);
    assertFalse(set.contains("A"));
    assertFalse(set.contains("B"));
    assertFalse(set.contains("C"));
  }

  public void testResizeIncrease() {
    TimeoutSet<String> set = new TimeoutSet<String>(3, 10);
    set.setTimeSource(adjustableTimeSource);

    set.add("A");
    set.add("B");
    set.add("C");
    assertTrue(set.contains("A"));
    assertTrue(set.contains("B"));
    assertTrue(set.contains("C"));

    set.add("D");
    assertFalse(set.contains("A"));
    assertTrue(set.contains("B"));
    assertTrue(set.contains("C"));
    assertTrue(set.contains("D"));

    set.resize(4);
    set.add("A");
    assertTrue(set.contains("A"));
    assertTrue(set.contains("B"));
    assertTrue(set.contains("C"));
    assertTrue(set.contains("D"));
  }

  public void testResizeDecrease() {
    TimeoutSet<String> set = new TimeoutSet<String>(4, 10);
    set.setTimeSource(adjustableTimeSource);

    set.add("A", 10);
    set.add("B",  5);
    set.add("C", 20);
    set.add("D", 15);

    set.resize(2);
    assertTrue(set.contains("C"));
    assertTrue(set.contains("D"));
    assertFalse(set.contains("A"));
    assertFalse(set.contains("B"));

    set.resize(1);
    assertTrue(set.contains("C"));
    assertFalse(set.contains("D"));
  }

  public void testSize() {
    TimeoutSet<String> set = new TimeoutSet<String>(4, 10);
    assertEquals(4, set.size());
    set.resize(10);
    assertEquals(10, set.size());
    set.resize(2);
    assertEquals(2, set.size());
  }

  public void testSetTimeout() {
    TimeoutSet<String> set = new TimeoutSet<String>(4, 5);
    set.setTimeSource(adjustableTimeSource);

    set.add("A");
    set.add("B");
    set.setTimeout(10);
    set.add("C");
    set.add("D");
    adjustableTimeSource.increaseTime(6);

    assertFalse(set.contains("A"));
    assertFalse(set.contains("B"));
    assertTrue(set.contains("C"));
    assertTrue(set.contains("D"));
  }
}
