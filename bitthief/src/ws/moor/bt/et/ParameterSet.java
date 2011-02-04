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

package ws.moor.bt.et;

import ws.moor.bt.util.CollectionUtils;
import ws.moor.bt.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO(pmoor): Javadoc
 */
public class ParameterSet {

  private final Map<String, String> map = new HashMap<String, String>();

  public void addParameter(String name, String value) {
    map.put(name, value);
  }

  public void addParameter(String name, long value) {
    addParameter(name, Long.toString(value));
  }

  public String toPostString() {
    Collection<String> values = CollectionUtils.mapNullRemoves(map.entrySet(),
        new CollectionUtils.Function<Map.Entry<String, String>, String>() {
      public String evaluate(Map.Entry<String, String> source) {
        try {
          StringBuilder result = new StringBuilder();
          result.append(URLEncoder.encode(source.getKey(), "UTF-8"));
          result.append("=");
          result.append(URLEncoder.encode(source.getValue(), "UTF-8"));
          return result.toString();
        } catch (UnsupportedEncodingException e) {
          return null;
        }
      }
    });
    return StringUtil.join(values, "&");
  }
}
