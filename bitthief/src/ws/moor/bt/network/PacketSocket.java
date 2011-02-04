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

package ws.moor.bt.network;

import ws.moor.bt.Environment;
import ws.moor.bt.network.packets.Packet;
import ws.moor.bt.network.packets.PacketHandler;
import ws.moor.bt.stats.CounterRepository;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;

/**
 * TODO(pmoor): Javadoc
 */
public interface PacketSocket {
  public void close() throws IOException;

  public void sendPacket(Packet packet) throws IOException;

  public void addPacketHandler(PacketHandler handler);

  public InetAddress getRemoteAddress();

  public int getRemotePort();

  public long getTimeSinceLastSend();

  public long getTimeSinceLastReceive();

  public boolean available();

  public String getInstrumentationKey();

  public void setCounterRepository(CounterRepository repository);

  public interface PacketSocketFactory {
    public PacketSocket createPacketSocket(SocketChannel channel, Environment environment) throws IOException;
  }
}
