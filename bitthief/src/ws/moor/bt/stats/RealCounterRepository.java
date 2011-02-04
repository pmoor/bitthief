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

import org.apache.log4j.Logger;
import ws.moor.bt.stats.recorder.BucketRecorder;
import ws.moor.bt.stats.recorder.LineAppender;
import ws.moor.bt.stats.recorder.LineAppenderRecorder;
import ws.moor.bt.stats.recorder.Recorder;
import ws.moor.bt.util.LoggingUtil;
import ws.moor.bt.util.SystemTimeSource;
import ws.moor.bt.util.TimeSource;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * TODO(pmoor): Javadoc
 */
public class RealCounterRepository implements CounterRepository {

  private final Properties properties;

  private Map<String, CounterImpl> counters =
      new HashMap<String, CounterImpl>();

  private Map<String, Map<String, CounterImpl>> keyedCounters =
      new HashMap<String, Map<String, CounterImpl>>();

  private Map<String, CounterImpl> implicitCounters =
      new HashMap<String, CounterImpl>();

  private static final Logger logger = LoggingUtil.getLogger(RealCounterRepository.class);

  private MinimalWriter statsWriter = new MinimalWriter() {
    public void write(String string) {
      // do nothing
    }
  };

  private TimeSource timeSource = SystemTimeSource.INSTANCE;

  private static final String STATISTICS_ATTRIBUTE = "statistics";

  private static final String KEYED_ATTRIBUTE = "keyed";

  public synchronized Counter getCounter(String name, String key) {
    return getCounterImpl(name, key);
  }

  public synchronized Counter getCounter(String name) {
    return getCounterImpl(name);
  }

  public CounterStatistics getStatistics(String name) {
    CounterImpl counter;
    if (isKeyedType(name)) {
      counter = getImplicitCounter(name);
    } else {
      counter = getCounterImpl(name);
    }
    return getCounterStatistics(counter);
  }

  public CounterStatistics getStatistics(String name, String key) {
    CounterImpl counter = getCounterImpl(name, key);
    return getCounterStatistics(counter);
  }

  public void setTimeSource(TimeSource timeSource) {
    this.timeSource = timeSource;
  }

  private CounterStatistics getCounterStatistics(CounterImpl counter) {
    CounterStatisticsSource source =
        lookForACounterStatisticsSource(counter.getRecorders());
    if (source == null) {
      // we should add counter statistics to this counter
      source = createACounterStatisticsSource(counter);
    }
    return source.createCounterStatistics();
  }

  private CounterStatisticsSource createACounterStatisticsSource(CounterImpl counter) {
    // TODO(pmoor): only add recorder if property file allows live stats
    BucketRecorder recorder = new BucketRecorder(timeSource.getTime(), counter.getName());
    counter.addRecorder(recorder);
    return recorder;
  }

  private CounterStatisticsSource lookForACounterStatisticsSource(Iterable<Recorder> recorders) {
    for (Recorder recorder : recorders) {
      if (recorder instanceof CounterStatisticsSource) {
        return (CounterStatisticsSource) recorder;
      }
    }
    return null;
  }

  private CounterImpl getCounterImpl(String name, String key) {
    if (key == null) {
      throw new IllegalArgumentException("supply a valid key");
    }
    CounterImpl counter = getExistingKeyedCounter(name, key);
    if (counter != null) {
      return counter;
    }
    return createNewKeyedCounter(name, key);
  }

  private CounterImpl getCounterImpl(String name) {
    CounterImpl counter = counters.get(name);
    if (counter != null) {
      return counter;
    }
    return createNewCounter(name);
  }

  private CounterImpl createNewCounter(String name) {
    assertNotKeyedType(name);
    CounterImpl counter = new CounterImpl(0, name, timeSource);
    addRecordersToCounter(counter, name, null);
    addContributonTargets(counter, name, null);
    counters.put(name, counter);
    return counter;
  }

  private void addRecordersToCounter(CounterImpl counter, String name, String key) {
    String fullName = getFullName(name, key);
    counter.addRecorder(createFileBasedRecorder(fullName));
  }

  private String getFullName(String name, String key) {
    StringBuilder result = new StringBuilder(name);
    if (key != null) {
      result.append("@").append(key);
    }
    return result.toString();
  }

  private Set<String> getCounterAttributes(String name) {
    String attributes = properties.getProperty(name);
    if (attributes == null) {
      throw new IllegalArgumentException("no definitions found for name: " + name);
    }
    Set<String> result = new HashSet<String>();
    for (String attribute : attributes.split(",")) {
      result.add(attribute.trim().toLowerCase());
    }
    return result;
  }

  private LineAppenderRecorder createFileBasedRecorder(String name) {
    return new LineAppenderRecorder(createLineAppender(name));
  }

  private LineAppender createLineAppender(final String name) {
    return new LineAppender() {
      public void append(String line) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append("\t").append(line).append("\n");
        statsWriter.write(builder.toString());
      }
    };
  }

  private void assertNotKeyedType(String name) {
    if (isKeyedType(name)) {
      throw new IllegalArgumentException("counter is keyed: " + name);
    }
  }

  private CounterImpl createNewKeyedCounter(String name, String key) {
    assertKeyedType(name);
    CounterImpl counter = new CounterImpl(0, getFullName(name, key), timeSource);
    counter.addParent(getImplicitCounter(name));
    addRecordersToCounter(counter, name, key);
    addContributonTargets(counter, name, key);
    addKeyedCounter(name, key, counter);
    return counter;
  }

  private void addKeyedCounter(String name, String key, CounterImpl counter) {
    Map<String, CounterImpl> map = keyedCounters.get(name);
    if (map == null) {
      map = new HashMap<String, CounterImpl>();
      keyedCounters.put(name, map);
    }
    map.put(key, counter);
  }

  private void addContributonTargets(CounterImpl counter, String name, String key) {
    String contributions = properties.getProperty(name + ".contribute");
    if (contributions != null) {
      for (String contribution : contributions.split(",")) {
        contribution = contribution.trim();
        if (contribution.length() == 0) {
          continue;
        }
        if (isKeyedType(contribution)) {
          if (key == null) {
            throw new IllegalArgumentException("a key is needed");
          }
          counter.addParent(getCounter(contribution, key));
        } else {
          counter.addParent(getCounter(contribution));
        }
      }
    }
  }

  private CounterImpl getImplicitCounter(String name) {
    CounterImpl counter = implicitCounters.get(name);
    if (counter != null) {
      return counter;
    }

    assertKeyedType(name);
    counter = new CounterImpl(0, name, timeSource);
    addRecordersToCounter(counter, name, null);

    implicitCounters.put(name, counter);
    return counter;
  }

  private void assertKeyedType(String name) {
    if (!isKeyedType(name)) {
      throw new IllegalArgumentException("counter is not keyed: " + name);
    }
  }

  private boolean isKeyedType(String name) {
    return getCounterAttributes(name).contains(KEYED_ATTRIBUTE);
  }

  private CounterImpl getExistingKeyedCounter(String name, String key) {
    Map<String, CounterImpl> map = keyedCounters.get(name);
    return map != null ? map.get(key) : null;
  }

  RealCounterRepository(Properties properties) {
    this.properties = properties;
  }

  public static RealCounterRepository fromResource(String resourceName) {
    InputStream stream = ClassLoader.getSystemResourceAsStream(resourceName);
    if (stream == null) {
      return null;
    }
    Properties properties = new Properties();
    try {
      properties.load(stream);
    } catch (IOException e) {
      return null;
    }
    return new RealCounterRepository(properties);
  }

  public Collection<String> getKeys(String name) {
    assertKeyedType(name);

    ArrayList<String> result = new ArrayList<String>();

    Map<String, CounterImpl> map = keyedCounters.get(name);
    if (map != null) {
      result.addAll(map.keySet());
    }
    return result;
  }

  public void setFileForWriting(String filename) {
    try {
      final FileWriter fileWriter = new FileWriter(filename, true);
      statsWriter = new MinimalWriter() {
        public void write(String string) throws IOException {
          fileWriter.write(string);
          fileWriter.flush();
        }
      };
    } catch (IOException e) {
      logger.fatal("Could Not Set Statistics File", e);
    }
  }

  private interface MinimalWriter {
    public void write(String string) throws IOException;
  }
}
