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

package ws.moor.bt.bencoding;

import java.util.Map;


public class BFormatter {

  public String prettyPrint(BEntity entity) {
    StringBuilder builder = new StringBuilder();
    print(builder, "", entity);
    return builder.toString();
  }

  private void print(StringBuilder builder, String prefix, BEntity entity) {
    if (entity instanceof BString) {
      printString(builder, (BString) entity);
    } else if (entity instanceof BInteger) {
      printInteger(builder, (BInteger) entity);
    } else if (entity instanceof BList) {
      printList(builder, prefix, (BList<BEntity>) entity);
    } else if (entity instanceof BDictionary) {
      printDictionary(builder, prefix, (BDictionary<BEntity>) entity);
    } else {
      throw new IllegalArgumentException();
    }
  }

  private void printInteger(StringBuilder builder, BInteger integer) {
    builder.append(integer.toString());
  }

  private void printString(StringBuilder builder, BString string) {
    boolean welldefined = true;
    byte[] chars = string.getBytes();
    for (byte c : chars) {
      if (!Character.isDefined(c)) {
        welldefined = false;
      }
    }
    if (welldefined) {
      builder.append(string.toString());
    } else {
      builder.append("<").append(string.getBytes().length).append(" binary bytes>");
    }
  }

  private void printList(StringBuilder builder, String prefix, BList<BEntity> list) {
    builder.append("[\n");
    for (BEntity entity : list) {
      builder.append(prefix).append("\t");
      print(builder, prefix + "\t", entity);
      builder.append("\n");
    }
    builder.append(prefix);
    builder.append("]");
  }

  private void printDictionary(StringBuilder builder, String prefix, BDictionary<BEntity> dictionary) {
    builder.append("{\n");
    for (Map.Entry<BString, BEntity> entry : dictionary.entrySet()) {
      builder.append(prefix).append("\t");
      printString(builder, entry.getKey());
      builder.append(" -> ");

      print(builder, prefix + "\t" + createWhitespace(entry.getKey().toString().length() + 4), entry.getValue());
      builder.append("\n");
    }
    builder.append(prefix).append("}");
  }

  private String createWhitespace(int i) {
    String result = "";
    while (i > 0) {
      result += " ";
      i--;
    }
    return result;
  }
}
