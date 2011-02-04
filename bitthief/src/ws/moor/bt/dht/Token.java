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

package ws.moor.bt.dht;

import ws.moor.bt.util.ByteArray;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.DigestUtil;
import ws.moor.bt.util.SystemTimeSource;
import ws.moor.bt.util.TimeSource;

import java.net.InetSocketAddress;

/**
 * TODO(pmoor): Javadoc
 */
public class Token extends ByteArray {

  private static final int SECRET_LENGTH = 20;
  private static final byte[][] SECRETS = new byte[][] {
      ByteUtil.randomByteArray(SECRET_LENGTH),
      ByteUtil.randomByteArray(SECRET_LENGTH),
      ByteUtil.randomByteArray(SECRET_LENGTH)};
  private static long SECRET_LAST_CHANGED = 0;
  public static final long SECRET_REFRESH_INTERVAL = 60 * 1000;

  public static TimeSource timeSource = SystemTimeSource.INSTANCE;

  public Token(byte[] bytes) {
    super(bytes);
  }

  public static Token createRandom() {
    return new Token(ByteUtil.randomByteArray(SECRET_LENGTH));
  }

  public static Token createForAddress(InetSocketAddress address) {
    return createWithSecret(address, getCurrentSecret());
  }

  public static boolean isValid(Token token, InetSocketAddress address) {
    for (byte[] secret : getValidSecrets()) {
      if (token.equals(createWithSecret(address, secret))) {
        return true;
      }
    }
    return false;
  }

  private static byte[] getCurrentSecret() {
    updateSecrets();
    return SECRETS[0];
  }

  private static byte[][] getValidSecrets() {
    updateSecrets();
    return SECRETS;
  }

  private static synchronized void updateSecrets() {
    if (timeSource.getTime() > SECRET_LAST_CHANGED + SECRET_REFRESH_INTERVAL) {
      System.arraycopy(SECRETS, 0, SECRETS, 1, SECRETS.length - 1);
      SECRETS[0] = ByteUtil.randomByteArray(SECRET_LENGTH);
      SECRET_LAST_CHANGED = timeSource.getTime();
    }
  }

  private static Token createWithSecret(InetSocketAddress address, byte[] secret) {
    byte[] src = new byte[SECRET_LENGTH];
    System.arraycopy(secret, 0, src, 0, SECRET_LENGTH);
    byte[] addressBytes = address.getAddress().getAddress();
    System.arraycopy(addressBytes, 0, src, 0, addressBytes.length);
    src[SECRET_LENGTH - 1] ^= (byte) (address.getPort() & 0xff);
    src[SECRET_LENGTH - 2] ^= (byte) ((address.getPort() >> 8) & 0xff);
    return new Token(DigestUtil.sha1(src));
  }
}
