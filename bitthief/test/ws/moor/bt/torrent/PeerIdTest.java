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

package ws.moor.bt.torrent;

import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class PeerIdTest extends ExtendedTestCase {

  public void testIt() {
    PeerId id = PeerId.createRandomMainlineId();

    String stringId = new String(id.getBytes());
    assertEquals(PeerId.LENGTH, stringId.length());
    assertTrue(stringId.startsWith("M4-4-0--"));
    assertEquals(20, PeerId.LENGTH);
  }

  public void testDistance() {
    PeerId a = PeerId.createRandomMainlineId();
    PeerId b = PeerId.createRandomMainlineId();
    assertEquals(ByteUtil.distance(a.getBytes(), b.getBytes()), a.distance(b));
    assertEquals(ByteUtil.distance(a.getBytes(), b.getBytes()), b.distance(a));
  }

  public void testFlipLastBit() {
    byte[] allZeros = new byte[PeerId.LENGTH];
    PeerId id = new PeerId(allZeros);

    PeerId id2 = id.flipLastBit();
    assertEquals(1, id2.getBytes()[19]);
    
    PeerId id3 = id2.flipLastBit();
    assertEquals(id, id3);
  }
}
