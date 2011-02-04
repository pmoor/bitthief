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

import org.easymock.classextension.EasyMock;
import ws.moor.bt.util.ExtendedTestCase;

import java.io.IOException;

/**
 * TODO(pmoor): Javadoc
 */
public class LineAppenderRecorderTest extends ExtendedTestCase {

  private LineAppender appender;
  private LineAppenderRecorder lineAppenderRecorder;

  protected void setUp() throws Exception {
    super.setUp();

    appender = EasyMock.createMock(LineAppender.class);

    lineAppenderRecorder = new LineAppenderRecorder(appender, 1000);
  }

  public void testRecordBigEnoughIntervals() throws IOException {
    appender.append("0\t1");
    appender.append("1000\t2");
    appender.append("2000\t3");
    EasyMock.replay(appender);

    lineAppenderRecorder.record(0, 1);
    lineAppenderRecorder.record(1000, 2);
    lineAppenderRecorder.record(2000, 3);

    EasyMock.verify(appender);
  }

  public void testRecordSmallIntervals() throws IOException {
    appender.append("0\t1");
    appender.append("1000\t3");
    appender.append("2000\t5");
    EasyMock.replay(appender);

    lineAppenderRecorder.record(0, 1);
    lineAppenderRecorder.record(500, 2);
    lineAppenderRecorder.record(1000, 3);
    lineAppenderRecorder.record(1500, 4);
    lineAppenderRecorder.record(2000, 5);

    EasyMock.verify(appender);
  }

  public void testRecordSmallIntervals2() throws IOException {
    appender.append("0\t1");
    appender.append("1000\t6");
    appender.append("2000\t8");
    appender.append("2005\t9");
    appender.append("5000\t10");
    EasyMock.replay(appender);

    lineAppenderRecorder.record(0, 1);
    lineAppenderRecorder.record(100, 2);
    lineAppenderRecorder.record(200, 3);
    lineAppenderRecorder.record(500, 4);
    lineAppenderRecorder.record(999, 5);
    lineAppenderRecorder.record(1000, 6);
    lineAppenderRecorder.record(1001, 7);
    lineAppenderRecorder.record(2000, 8);
    lineAppenderRecorder.record(2005, 9);
    lineAppenderRecorder.record(5000, 10);

    EasyMock.verify(appender);
  }
}
