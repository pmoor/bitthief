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

package ws.moor.bt.util;

import java.util.Collection;

/**
 * TODO(pmoor): Javadoc
 */
public class StringUtil {
  public static String join(Collection elements, String separator) {
    if (elements == null) {
      throw new NullPointerException();
    }
    if (separator == null) {
      separator = "";
    }

    boolean first = true;
    StringBuilder stringBuilder = new StringBuilder();
    for (Object element : elements) {
      if (!first) {
        stringBuilder.append(separator);
      } else {
        first = false;
      }
      stringBuilder.append(element);
    }
    return stringBuilder.toString();
  }

  public static String repeat(String pattern, int repetitions) {
    StringBuilder result = new StringBuilder(repetitions * pattern.length());
    for (int i = 0; i < repetitions; i++) {
      result.append(pattern);
    }
    return result.toString();
  }
}
