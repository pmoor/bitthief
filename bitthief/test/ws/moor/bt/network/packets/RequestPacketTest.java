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
public class RequestPacketTest extends AbstractRequestCancelPacketTest {

  protected RequestPacket constructPacket(int index, int begin, int length) {
    return new RequestPacket(index, begin, length);
  }

  public void testID() {
    assertEquals(6, RequestPacket.ID);
    assertEquals(RequestPacket.ID, packet.getId());
  }

  protected PacketConstructor<RequestPacket> constructConstructor() {
    return RequestPacket.getConstructor();
  }

  public void testHandler() {
    PacketHandler handler = EasyMock.createMock(PacketHandler.class);
    handler.handleRequestPacket(EasyMock.same((RequestPacket) packet));
    EasyMock.replay(handler);
    packet.handle(handler);
    EasyMock.verify(handler);
  }

  public void testAlternativeConstructor() {
    RequestPacket requestPacket = new RequestPacket(new Block(47, 55, 16384));
    assertEquals(new RequestPacket(47, 55, 16384), requestPacket);
  }

   public void testGetRequestedBlock() {
    RequestPacket requestPacket = new RequestPacket(new Block(47, 55, 16384));
    assertEquals(new Block(47, 55, 16384), requestPacket.getBlock());
  }
}
