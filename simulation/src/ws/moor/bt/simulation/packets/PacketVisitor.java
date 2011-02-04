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

package ws.moor.bt.simulation.packets;

/**
 * Created by IntelliJ IDEA.
 * User: pmoor
 * Date: Aug 29, 2006
 * Time: 1:39:12 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PacketVisitor {

  void visitAvailableTuplesPacket(AvailableTuplesPacket availableTuples);

  void visitTuplePacket(TuplePacket tuplePacket);

  void visitTupleRequestPacket(TupleRequestPacket requestPacket);

  void visitTupleAvailablePacket(TupleAvailablePacket tupleAvailablePacket);

  void visitResetAvailableTuplesPacket(ResetAvailableTuplesPacket packet);
}
