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

package ws.moor.bt.network.packets;

import org.apache.log4j.Logger;
import ws.moor.bt.downloader.Block;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.LoggingUtil;

/**
 * TODO(pmoor): Javadoc
 */
public abstract class AbstractRequestCancelPacket implements Packet {

  protected static final int PAYLOAD_LENGTH = 12;
  protected static final Logger logger = LoggingUtil.getLogger(AbstractRequestCancelPacket.class);

  private final int index;
  private final int begin;
  private final int length;

  public AbstractRequestCancelPacket(int index, int begin, int length) {
    assertValidOffsets(index, begin, length);
    this.index = index;
    this.begin = begin;
    this.length = length;
  }

  private void assertValidOffsets(int index, int begin, int length) {
    if (index < 0 || begin < 0 || length < 0) {
      throw new IllegalArgumentException();
    }
    if (length != 16384) {
      logger.debug("non-standard request length: " + length);
    }
  }

  public int writeIntoBuffer(byte[] buffer, int offset) {
    ByteUtil.int_to_b32(index, buffer, offset);
    ByteUtil.int_to_b32(begin, buffer, offset + 4);
    ByteUtil.int_to_b32(length, buffer, offset + 8);
    return offset + PAYLOAD_LENGTH;
  }

  public int getPayloadLength() {
    return PAYLOAD_LENGTH;
  }

  public abstract int getId();

  public int getIndex() {
    return index;
  }

  public int getBegin() {
    return begin;
  }

  public int getLength() {
    return length;
  }

  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final AbstractRequestCancelPacket that = (AbstractRequestCancelPacket) o;
    if (begin != that.begin) {
      return false;
    }
    if (index != that.index) {
      return false;
    }
    return length == that.length;
  }

  public int hashCode() {
    return 29 * (29 * index + begin) + length;
  }

  public Block getBlock() {
    return new Block(getIndex(), getBegin(), getLength());
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getIndex());
    builder.append(":");
    builder.append(getBegin());
    builder.append("-");
    builder.append(getBegin() + getLength());
    return builder.toString();
  }
}
