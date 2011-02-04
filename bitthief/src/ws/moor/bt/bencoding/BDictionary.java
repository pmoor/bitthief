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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BDictionary<V extends BEntity> implements BEntity, Map<BString, V> {

  final static char CODE = 'd';
  final static char END_CODE = 'e';

  private final Map<BString, V> map = new LinkedHashMap<BString, V>();

  public V getByString(String key) {
    return map.get(new BString(key));
  }

  public int size() {
    return map.size();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  public V get(Object key) {
    return map.get(key);
  }

  public V put(BString key, V value) {
    return map.put(key, value);
  }

  public V remove(Object key) {
    return map.remove(key);
  }

  public void putAll(Map<? extends BString, ? extends V> t) {
    map.putAll(t);
  }

  public void clear() {
    map.clear();
  }

  public Set<BString> keySet() {
    return map.keySet();
  }

  public Collection<V> values() {
    return map.values();
  }

  public Set<Entry<BString, V>> entrySet() {
    return map.entrySet();
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof BDictionary)) {
      return false;
    }
    BDictionary other = (BDictionary) obj;
    return map.equals(other.map);
  }

  public int hashCode() {
    return map.hashCode();
  }

  public String toString() {
    return map.toString();
  }

  public void encode(OutputStream os) throws IOException {
    os.write(CODE);
    for (Entry<BString, V> entries : getSortedEntries()) {
      entries.getKey().encode(os);
      entries.getValue().encode(os);
    }
    os.write(END_CODE);
  }

  private List<Entry<BString, V>> getSortedEntries() {
    List<Entry<BString, V>> list =
        new ArrayList<Entry<BString, V>>(map.entrySet());
    Collections.sort(list, new Comparator<Entry<BString, V>>() {
      public int compare(Entry<BString, V> o1, Entry<BString, V> o2) {
        return o1.getKey().toString().compareTo(o2.getKey().toString());
      }
    });
    return list;
  }

  public byte[] encode() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    encode(baos);
    return baos.toByteArray();
  }

  static BDictionary<BEntity> parse(PushbackInputStream stream) throws IOException, ParseException {
      BDictionary<BEntity> dictionary = new BDictionary<BEntity>();
      int read = stream.read();
      byte code = BDecoder.checkValidRead(read);
      while (code != BDictionary.END_CODE) {
        stream.unread(read);
        BEntity key = BDecoder.parse(stream);
        if (!(key instanceof BString)) {
          throw new IOException("key must be a string, but was a " + key.getClass().getName());
        }
        BEntity value = BDecoder.parse(stream);
        dictionary.put((BString) key, value);
        read = stream.read();
        code = BDecoder.checkValidRead(read);
      }
      return dictionary;
    }
}
