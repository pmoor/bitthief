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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class BList<E extends BEntity> implements List<E>, BEntity {

  final static char CODE = 'l';
  final static char END_CODE = 'e';

  private final List<E> list = new ArrayList<E>();

  public boolean add(E o) {
    return list.add(o);
  }

  public void add(int index, E element) {
    list.add(index, element);
  }

  public boolean addAll(Collection<? extends E> c) {
    return list.addAll(c);
  }

  public boolean addAll(int index, Collection<? extends E> c) {
    return list.addAll(index, c);
  }

  public void clear() {
    list.clear();
  }

  public boolean contains(Object o) {
    return list.contains(o);
  }

  public boolean containsAll(Collection<?> c) {
    return list.containsAll(c);
  }

  public boolean equals(Object o) {
    if (!(o instanceof BList)) {
      return false;
    }
    BList other = (BList) o;
    return list.equals(other.list);
  }

  public E get(int index) {
    return list.get(index);
  }

  public int hashCode() {
    return list.hashCode();
  }

  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public Iterator<E> iterator() {
    return list.iterator();
  }

  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  public ListIterator<E> listIterator() {
    return list.listIterator();
  }

  public ListIterator<E> listIterator(int index) {
    return list.listIterator(index);
  }

  public E remove(int index) {
    return list.remove(index);
  }

  public boolean remove(Object o) {
    return list.remove(o);
  }

  public boolean removeAll(Collection<?> c) {
    return list.removeAll(c);
  }

  public boolean retainAll(Collection<?> c) {
    return list.retainAll(c);
  }

  public E set(int index, E element) {
    return list.set(index, element);
  }

  public int size() {
    return list.size();
  }

  public List<E> subList(int fromIndex, int toIndex) {
    return list.subList(fromIndex, toIndex);
  }

  public Object[] toArray() {
    return list.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return list.toArray(a);
  }

  public String toString() {
    return list.toString();
  }

  public void encode(OutputStream os) throws IOException {
    os.write(CODE);
    for (E item : list) {
      item.encode(os);
    }
    os.write(END_CODE);
  }

  public byte[] encode() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    encode(baos);
    return baos.toByteArray();
  }

  static BList<BEntity> parse(PushbackInputStream stream) throws IOException, ParseException {
    BList<BEntity> list = new BList<BEntity>();
    int read = stream.read();
    byte code = BDecoder.checkValidRead(read);
    while (code != BList.END_CODE) {
      stream.unread(read);
      list.add(BDecoder.parse(stream));
      read = stream.read();
      code = BDecoder.checkValidRead(read);
    }
    return list;
  }
}
