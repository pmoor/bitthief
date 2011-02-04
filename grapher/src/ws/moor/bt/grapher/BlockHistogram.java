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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BlockHistogram {

  public static void main(String[] args) throws IOException {
    File dir = new File("/home/pmoor/Desktop");
    for (File file : dir.listFiles()) {
      if (!file.getName().startsWith("BitThief-Run-")) {
        continue;
      }
      processFile(new File(file, "stats.csv"));
    }
  }

  private static void processFile(File file) throws IOException {
    System.out.println("processing " + file);
    CSVInputStream in = new CSVInputStream(
        new FileInputStream(file));

    CSVNamePrefixFilter filtered = new CSVNamePrefixFilter(in, "torrent.blocks.seed@");


    final Map<String, Long> map = new HashMap<String, Long>();
    while (filtered.hasMoreEntries()) {
      CSVEntry next = filtered.nextEntry();
      map.put(extractIPfromName(next), next.getValue());
    }

    List<String> sortedKeys = new ArrayList<String>(map.keySet());
    Collections.sort(sortedKeys, new Comparator<String>() {
      public int compare(String o1, String o2) {
        return (int) (map.get(o2) - map.get(o1));
      }
    });

    File outputFile = new File(file.getParentFile(), "processed-seed.csv");
    FileWriter writer = new FileWriter(outputFile);
    for (String key : sortedKeys) {
      writer.append(key + "\t" + map.get(key) + "\n");
    }
    writer.close();
  }

  private static String extractIPfromName(CSVEntry next) {
    StringTokenizer tokenizer = new StringTokenizer(next.getName(), "@");
    if (tokenizer.hasMoreTokens()) {
      tokenizer.nextToken();
      if (tokenizer.hasMoreTokens()) {
        return tokenizer.nextToken();
      }
    }
    return "error";
  }
}
