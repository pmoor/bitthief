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

package ws.moor.bt.stats.recorder;

import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class BucketTest extends ExtendedTestCase {

  public void testSomeGetRanges() {
    Bucket bucket = new Bucket(10, 1000, 10 * 1000);

    assertEquals(0, bucket.get( 9999));
    assertEquals(0, bucket.get(10000));
    assertEquals(0, bucket.get(10999));
    assertEquals(0, bucket.get(11000));

    assertEquals(0, bucket.get(1000));
    assertEquals(0, bucket.get(999));
  }

  public void testGetOldestAndNewestTime() {
    Bucket bucket = new Bucket(10, 1000, 0);
    assertEquals(0, bucket.getOldestTime());
    assertEquals(999, bucket.getNewestTime());

    bucket.addValue(9000, 1);
    assertEquals(0, bucket.getOldestTime());
    assertEquals(9999, bucket.getNewestTime());

    bucket.addValue(10000, 1);
    assertEquals(1000, bucket.getOldestTime());
    assertEquals(10999, bucket.getNewestTime());

    bucket.addValue(20000, 1);
    assertEquals(11000, bucket.getOldestTime());
    assertEquals(20999, bucket.getNewestTime());
  }

  public void testSomeSetting() {
    Bucket bucket = new Bucket(10, 1000, 0);

    for (int i = 0; i < 10; i++) {
      bucket.addValue(i * 1000, 10 + i);
      bucket.addValue(i * 1000 + 200, 15 + i);
      bucket.addValue(i * 1000 + 999, 20 + i);
    }

    for (int i = 0; i < 10; i++) {
      assertEquals(15 + i, bucket.get(i * 1000));
    }

    assertEquals(29, bucket.get(10000));
    assertEquals(15, bucket.get(-1));
  }

  public void testWrapping() {
    Bucket bucket = new Bucket(2, 1000, 0);

    bucket.addValue(500, 1);
    bucket.addValue(1500, 2);
    assertEquals(1, bucket.get(500));
    assertEquals(2, bucket.get(1500));
    assertEquals(2, bucket.get(2500));

    bucket.addValue(2500, 3);
    assertEquals(2, bucket.get(500));
    assertEquals(3, bucket.get(2500));
    assertEquals(3, bucket.get(3500));

    bucket.addValue(4500, 4);
    assertEquals(3, bucket.get(2500));
    assertEquals(3, bucket.get(3500));
    assertEquals(4, bucket.get(4500));
  }

  public void testDerivatives() {
    Bucket bucket = new Bucket(2, 1000, 0);

    bucket.addValue(333, 2);
    bucket.addValue(666, 4);
    bucket.addValue(999, 6);
    assertEquals(0d, bucket.getDerivative(-1));
    assertEquals(2d/1000, bucket.getDerivative(500));
    assertEquals(0d, bucket.getDerivative(1500));

    bucket.addValue(1333, 8);
    assertEquals(8, bucket.get(1500));
    assertEquals(0d, bucket.getDerivative(-1));
    assertEquals(4d/1000, bucket.getDerivative(500));
    assertEquals(4d/1000, bucket.getDerivative(1500));
    assertEquals(0d, bucket.getDerivative(2000));
    assertEquals(8, bucket.get(2000));
  }

  public void testGetOldestTimeContinuousWrap() {
    Bucket array = new Bucket(5, 1, 7);
    assertEquals(7, array.getOldestTime());

    array.addValue(8, 8);
    assertEquals(7, array.getOldestTime());

    array.addValue(11, 11);
    assertEquals(7, array.getOldestTime());

    array.addValue(12, 12);
    assertEquals(8, array.getOldestTime());

    array.addValue(13, 13);
    assertEquals(9, array.getOldestTime());
  }

  public void testGetOldestTimeNonContinuousWrap() {
    Bucket array = new Bucket(5, 1, 7);
    assertEquals(7, array.getOldestTime());

    array.addValue(8, 8);
    assertEquals(7, array.getOldestTime());

    array.addValue(11, 11);
    assertEquals(7, array.getOldestTime());

    array.addValue(13, 13);
    assertEquals(9, array.getOldestTime());

    array.addValue(14, 14);
    assertEquals(10, array.getOldestTime());
  }
}
