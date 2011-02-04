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

package ws.moor.bt.stats;

import ws.moor.bt.util.ExtendedTestCase;

import java.util.Collection;
import java.util.Properties;

/**
 * TODO(pmoor): Javadoc
 */
public class CounterRepositoryTest extends ExtendedTestCase {

  private CounterRepository repository;

  protected void setUp() throws Exception {
    super.setUp();

    Properties properties = new Properties();
    properties.setProperty("a", "keyed, statistics");
    properties.setProperty("b", "counter");
    properties.setProperty("c", "statistics");
    properties.setProperty("c.contribute", "d");
    properties.setProperty("d", "counter");

    repository = new RealCounterRepository(properties);
  }

  public void testGetUnkeyed() {
    Counter counterA = repository.getCounter("b");
    Counter counterB = repository.getCounter("c");

    assertNotSame(counterA, counterB);
    assertSame(counterA, repository.getCounter("b"));
  }

  public void testGetUnkeyedWithKey() {
    try {
      repository.getCounter("b", "1");
      fail("should fail");
    } catch (Exception e) {
      // expected
    }
  }

  public void testGetKeyed() {
    Counter counterA = repository.getCounter("a", "1");
    Counter counterB = repository.getCounter("a", "2");

    assertNotSame(counterA, counterB);
    assertSame(counterA, repository.getCounter("a", "1"));
  }

  public void testGetKeyedWithoutKey() {
    try {
      repository.getCounter("a");
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testKeyedStatistics() {
    repository.getCounter("a", "1").set(13);
    repository.getCounter("a", "2").set(2);
    repository.getCounter("a", "3").set(5);
    CounterStatistics statistics = repository.getStatistics("a");

    assertEquals(20, statistics.latestValue());
  }

  public void testStatistics() {
    repository.getCounter("c").set(18);
    assertEquals(18, repository.getStatistics("c").latestValue());

    repository.getCounter("b").set(42);
    assertEquals(42, repository.getStatistics("b").latestValue());
  }

  public void testContribution() {
    Counter counterA = repository.getCounter("c");
    Counter counterB = repository.getCounter("d");

    counterA.set(15);
    assertEquals(15, counterB.get());
    counterA.decrease(3);
    assertEquals(12, counterB.get());
  }

  public void testGetKeys() {
    repository.getCounter("a", "1");
    repository.getCounter("a", "2");
    repository.getCounter("a", "3");
    repository.getCounter("a", "4");

    Collection<String> keys = repository.getKeys("a");
    assertEquals(4, keys.size());
    assertContains("1", keys);
    assertContains("2", keys);
    assertContains("3", keys);
    assertContains("4", keys);
  }

  public void testGetKeysForNonKeyed() {
    try {
      repository.getKeys("b");
      fail("should fail");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testGetKeysForEmptyCounter() {
    assertTrue(repository.getKeys("a").isEmpty());
  }
}
