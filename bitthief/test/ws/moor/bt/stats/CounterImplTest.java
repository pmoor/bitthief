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

import org.easymock.classextension.EasyMock;
import ws.moor.bt.stats.recorder.Recorder;
import ws.moor.bt.util.AdjustableTimeSource;
import ws.moor.bt.util.ExtendedTestCase;
import ws.moor.bt.util.SystemTimeSource;

/**
 * TODO(pmoor): Javadoc
 */
public class CounterImplTest extends ExtendedTestCase {

  private CounterImpl counter;

  public void testCreation() {
    assertEquals(42, counter.get());
  }

  public void testSetting() {
    counter.set(7);
    assertEquals(7, counter.get());
    counter.set(941);
    assertEquals(941, counter.get());
  }

  public void testIncrease() {
    counter.set(100);
    assertEquals(110, counter.increase(10));
    assertEquals(110, counter.get());
    assertEquals(160, counter.increase(50));
    assertEquals(160, counter.get());
  }

  public void testDecrease() {
    counter.set(100);
    assertEquals(50, counter.decrease(50));
    assertEquals(50, counter.get());
    assertEquals(10, counter.decrease(40));
    assertEquals(10, counter.get());
  }

  public void testAddRecorder() {
    Recorder recorder = EasyMock.createMock(Recorder.class);
    recorder.record(0, 0);
    EasyMock.replay(recorder);

    counter = new CounterImpl(0, "fake", new AdjustableTimeSource());
    counter.addRecorder(recorder);

    EasyMock.verify(recorder);
  }

  public void testUpdateRecorder() {
    Recorder recorder = EasyMock.createMock(Recorder.class);
    recorder.record(0, 0);
    recorder.record(40, 17);
    recorder.record(40, 50);
    recorder.record(50, 60);
    EasyMock.replay(recorder);

    AdjustableTimeSource timeSource = new AdjustableTimeSource();
    counter = new CounterImpl(0, "fake",timeSource);
    counter.addRecorder(recorder);
    timeSource.increaseTime(40);
    counter.increase(17);
    counter.increase(33);
    timeSource.increaseTime(10);
    counter.set(60);

    EasyMock.verify(recorder);
  }

  public void testParentChild() {
    CounterImpl parent = new CounterImpl(15, "fake");
    CounterImpl child = new CounterImpl(10, "fake");
    child.addParent(parent);
    CounterImpl grandchild = new CounterImpl(0, "fake");
    grandchild.addParent(child);

    assertEquals(25, parent.get());
    child.increase(10);
    assertEquals(35, parent.get());
    child.set(0);
    assertEquals(15, parent.get());

    grandchild.increase(10);
    assertEquals(10, child.get());
    assertEquals(25, parent.get());
  }

  protected void setUp() throws Exception {
    super.setUp();
    counter = new CounterImpl(42, "fake", SystemTimeSource.INSTANCE);
  }
}
