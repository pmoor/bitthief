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

import org.easymock.classextension.EasyMock;
import ws.moor.bt.downloader.Block;

/**
 * TODO(pmoor): Javadoc
 */
public class CancelPacketTest extends AbstractRequestCancelPacketTest {

  protected CancelPacket constructPacket(int index, int begin, int length) {
    return new CancelPacket(index, begin, length);
  }

  public void testID() {
    assertEquals(8, CancelPacket.ID);
    assertEquals(CancelPacket.ID, packet.getId());
  }

  protected PacketConstructor<CancelPacket> constructConstructor() {
    return CancelPacket.getConstructor();
  }

  public void testHandler() {
    PacketHandler handler = EasyMock.createMock(PacketHandler.class);
    handler.handleCancelPacket(EasyMock.same((CancelPacket) packet));
    EasyMock.replay(handler);
    packet.handle(handler);
    EasyMock.verify(handler);
  }

  public void testBlockConstructor() {
    assertEquals(new CancelPacket(841, 41, 1521523), new CancelPacket(new Block(841, 41, 1521523)));
  }

   public void testGetRequestedBlock() {
    CancelPacket cancelPacket = new CancelPacket(new Block(47, 55, 16384));
    assertEquals(new Block(47, 55, 16384), cancelPacket.getBlock());
  }
}
