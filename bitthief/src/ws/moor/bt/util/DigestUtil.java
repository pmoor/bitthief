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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * TODO(pmoor): Javadoc
 */
public class DigestUtil {

  private static MessageDigest sha1Digest;

  public static byte[] sha1(byte[] message) {
    MessageDigest md = getSha1Digest();
    return md.digest(message);
  }

  public static byte[] sha1(byte[] bytes, int offset, int length) {
    MessageDigest md = getSha1Digest();
    md.update(bytes, offset, length);
    return md.digest();
  }

  private static synchronized MessageDigest getSha1Digest() {
    if (sha1Digest == null) {
      try {
        sha1Digest = MessageDigest.getInstance("SHA-1");
      } catch (NoSuchAlgorithmException e) {
        return null;
      }
    }
    try {
      return (MessageDigest) sha1Digest.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }
}
