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

package ws.moor.bt.grapher;

import org.apache.log4j.Logger;
import ws.moor.bt.util.LoggingUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.GregorianCalendar;

/**
 * TODO(pmoor): Javadoc
 */
public abstract class Report {

  protected final CSVMapCollector collector;

  private final String name;
  private long runningTime;

  protected static final int WIDTH = 640;
  protected static final int HEIGHT = 400;
  private static final Logger logger = LoggingUtil.getLogger(Report.class);

  protected long getLastValue(String name) {
    CSVStream stream = collector.getStream(name);
    if (stream == null) {
      return Long.MIN_VALUE;
    }
    CSVEntry current = null;
    while (stream.hasMoreEntries()) {
      current = stream.nextEntry();
    }
    return current.getValue();
  }

  protected long getAverage(String name) {
    CSVStream stream = collector.getStream(name);
    if (stream == null) {
      return Long.MIN_VALUE;
    }
    long count = 0;
    long sum = 0;
    while (stream.hasMoreEntries()) {
      CSVEntry current = stream.nextEntry();
      sum += current.getValue();
      count++;

    }
    return sum / count;
  }

  public Report(File csvFile, String name) throws FileNotFoundException {
    this.name = name;
    logger.info("building map collector for " + name);
    collector = new CSVMapCollector(getCSVStream(csvFile));
  }

  private CSVStream getCSVStream(File csvFile) throws FileNotFoundException {
    CSVStream stream = new CSVInputStream(new FileInputStream(csvFile));
    CSVCollector collection = new CSVCollector(stream);
    final long startTime = collection.getFirst().getTime();
    long endTime = collection.getLast().getTime();
    runningTime = endTime - startTime;
    GregorianCalendar calendar = new GregorianCalendar(2006, 1, 5, 0, 0, 0);
    final long offset = calendar.getTimeInMillis();

    stream = new CSVMutator(collection.getStream()) {
      protected CSVEntry mutate(CSVEntry entry) {
        String name = entry.getName();
        if (name.endsWith("@null")) {
          name = name.substring(0, name.length() - 5);
          return new CSVEntry(name, entry.getTime(), entry.getValue());
        }
        return entry;
      }
    };
    stream = new CSVMutator(stream) {
      protected CSVEntry mutate(CSVEntry entry) {
        long time = entry.getTime() - startTime + offset;
        return new CSVEntry(entry.getName(), time, entry.getValue());
      }
    };
    return stream;
  }

  public void appendHTMLForGraph(StringBuilder builder,
                                 File file,
                                 String graphTitle,
                                 int width,
                                 int height) {
    builder.append("<p>\n");
    //builder.append("\t<h2>").append(graphTitle).append("</h2>\n");
    //builder.append("\t<hr/>\n");
    builder.append("\t<img src=\"").append(file.getAbsolutePath());
    builder.append("\" width=\"").append(width);
    builder.append("\" height=\"").append(height).append("\"/>\n");
    builder.append("</p>\n");
  }

  public abstract int getAverageSeederCount();
  public abstract int getAverageLeecherCount();

  public long getRunningTime() {
    return runningTime;
  }

  public double getSeederLeecherRatio() {
    return (double) getAverageSeederCount() / getAverageLeecherCount();
  }

  public String getName() {
    return name;
  }
}
