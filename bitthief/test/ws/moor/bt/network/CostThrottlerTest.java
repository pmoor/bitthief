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

package ws.moor.bt.network;

import ws.moor.bt.stats.FakeRepository;
import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class CostThrottlerTest extends ExtendedTestCase {

  private volatile long counter;
  private volatile int nextIndex;

  protected void setUp() throws Exception {
    super.setUp();
    counter = 0;
    nextIndex = 0;
  }

  public void testExpectation() throws InterruptedException {
    CostThrottler throttler = new CostThrottler(1000, 1000, new FakeRepository());

    long totalCosts = 0;
    for (int i = 0; i < 100; i++) {
      final int costs = rnd.nextInt(50);
      final int index = i;
      totalCosts += costs;
      throttler.submitJob(costs, new Runnable() {
        public void run() {
          if (nextIndex++ == index) {
            counter += costs;
          } else {
            counter += costs + 1;
          }
        }
      });
    }

    long timeStarted = System.currentTimeMillis();
    throttler.start();

    while (counter < totalCosts) {
      long diff = System.currentTimeMillis() - timeStarted;
      assertEquals(diff, counter, 100);
      Thread.sleep(100);
    }

    throttler.stop();

    if (counter > totalCosts) {
      fail();
    }
  }
}
