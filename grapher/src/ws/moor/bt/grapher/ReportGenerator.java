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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * TODO(pmoor): Javadoc
 */
public class ReportGenerator {

  public static void main(String[] args) throws IOException {
    File directory = new File("/local/pmoor");
    for (File dir : getTorrentDirectories(directory)) {
      //oldAnalysis(dir);
      for (File file : getMainlineRuns(dir)) {
        String name = dir.getName() + "/" + file.getName();
        analyzeReport(new ReportMainline(file, name));
      }
      for (File file : getBitThiefRuns(dir)) {
        String name = dir.getName() + "/" + file.getName();
        analyzeReport(new ReportBitThief(new File(file, "stats.csv"), name));
    }
  }
  }

  private static void analyzeReport(Report report) {
    StringBuilder message = new StringBuilder();
    message.append(report.getName()).append("\t");
    message.append(report.getSeederLeecherRatio()).append("\t");
    message.append(report.getRunningTime()).append("\n");
    System.out.print(message.toString());
  }

  private static void oldAnalysis(File dir) throws IOException {
    for (File file : getMainlineRuns(dir)) {
      String name = dir.getName() + ":" + file.getName();
      ReportMainline report = new ReportMainline(file, name);
      report.create(new File(dir, "Reports/" + file.getName()));
    }
    for (File file : getBitThiefRuns(dir)) {
      String name = dir.getName() + ":" + file.getName();
      ReportBitThief report = new ReportBitThief(new File(file, "stats.csv"), name);
      report.create(new File(dir, "Reports/" + file.getName()));
    }
  }

  private static File[] getTorrentDirectories(File directory) {
    return directory.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("Torrent_");
      }
    });
  }

  private static File[] getBitThiefRuns(final File directory) {
    return directory.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("BitThief-Run-");
      }
    });
  }

  private static File[] getMainlineRuns(final File directory) {
    return directory.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("Mainline-Run-") && name.endsWith(".stats");
      }
    });
  }
}
