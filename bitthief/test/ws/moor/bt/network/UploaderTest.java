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

import org.easymock.classextension.EasyMock;
import ws.moor.bt.downloader.Block;
import ws.moor.bt.network.packets.Packet;
import ws.moor.bt.network.packets.PiecePacket;
import ws.moor.bt.stats.FakeRepository;
import ws.moor.bt.storage.DataBlock;
import ws.moor.bt.storage.DataBlockProvider;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.ExtendedTestCase;

import java.io.IOException;

/**
 * TODO(pmoor): Javadoc
 */
public class UploaderTest extends ExtendedTestCase {

  private static final int BLOCK_SIZE = 16384;

  public void testCorrectlyQueueing() throws IOException, DataBlockProvider.BlockRefusedException {
    CostThrottler throttler = EasyMock.createMock(CostThrottler.class);
    throttler.submitJob(EasyMock.eq(BLOCK_SIZE), (Runnable) EasyMock.anyObject());
    EasyMock.expectLastCall().anyTimes();
    throttler.removeJob((Runnable) EasyMock.anyObject());
    EasyMock.expectLastCall().anyTimes();
    EasyMock.replay(throttler);

    PacketSocket socket = EasyMock.createMock(PacketSocket.class);
    EasyMock.expect(socket.getInstrumentationKey()).andReturn("key").anyTimes();
    EasyMock.replay(socket);

    DataBlockProvider blockProvider = EasyMock.createMock(DataBlockProvider.class);
    byte[] blockData = ByteUtil.randomByteArray(BLOCK_SIZE);
    DataBlock dataABlock = new DataBlock(15, 7, blockData);
    EasyMock.expect(blockProvider.getBlock(EasyMock.eq(new Block(15, 7, BLOCK_SIZE)))).andReturn(dataABlock);
    DataBlock dataBBlock = new DataBlock(18, 22, blockData);
    EasyMock.expect(blockProvider.getBlock(EasyMock.eq(new Block(18, 22, BLOCK_SIZE)))).andReturn(dataBBlock);
    EasyMock.replay(blockProvider);

    Uploader uploader =
        new Uploader(throttler, socket, blockProvider, new FakeRepository());

    uploader.addRequest(new Block(15, 7, BLOCK_SIZE));
    uploader.addRequest(new Block(18, 22, BLOCK_SIZE));

    EasyMock.verify(socket);
    EasyMock.reset(socket);
    PiecePacket packet = new PiecePacket(15, 7, blockData);
    socket.sendPacket(EasyMock.eq(packet));
    EasyMock.expect(socket.getInstrumentationKey()).andReturn("key").anyTimes();
    EasyMock.replay(socket);

    uploader.run();

    EasyMock.verify(socket);
    EasyMock.reset(socket);
    packet = new PiecePacket(18, 22, blockData);
    socket.sendPacket(EasyMock.eq(packet));
    EasyMock.expect(socket.getInstrumentationKey()).andReturn("key").anyTimes();
    EasyMock.replay(socket);

    uploader.run();

    EasyMock.verify(socket);
    EasyMock.verify(blockProvider);
    EasyMock.verify(throttler);
  }

  public void testRefusedRequests() throws DataBlockProvider.BlockRefusedException, IOException {
    CostThrottler throttler = EasyMock.createMock(CostThrottler.class);
    throttler.submitJob(EasyMock.eq(BLOCK_SIZE), (Runnable) EasyMock.anyObject());
    EasyMock.expectLastCall().anyTimes();
    throttler.removeJob((Runnable) EasyMock.anyObject());
    EasyMock.expectLastCall().anyTimes();
    EasyMock.replay(throttler);

    PacketSocket socket = EasyMock.createMock(PacketSocket.class);
    EasyMock.expect(socket.getInstrumentationKey()).andReturn("key").anyTimes();
    socket.sendPacket((Packet) EasyMock.anyObject());
    EasyMock.replay(socket);

    DataBlockProvider blockProvider = EasyMock.createMock(DataBlockProvider.class);
    byte[] blockData = ByteUtil.randomByteArray(BLOCK_SIZE);
    DataBlock dataABlock = new DataBlock(15, 7, blockData);
    EasyMock.expect(blockProvider.getBlock(EasyMock.eq(new Block(15, 7, BLOCK_SIZE)))).andReturn(dataABlock);
    DataBlock dataBBlock = new DataBlock(18, 22, blockData);
    EasyMock.expect(blockProvider.getBlock(EasyMock.eq(new Block(18, 22, BLOCK_SIZE)))).andThrow(
        new DataBlockProvider.BlockRefusedException(""));
    EasyMock.replay(blockProvider);

    Uploader uploader =
        new Uploader(throttler, socket, blockProvider, new FakeRepository());

    uploader.addRequest(new Block(15, 7, BLOCK_SIZE));
    uploader.addRequest(new Block(18, 22, BLOCK_SIZE));

    assertFalse(uploader.onlyRefusedRequestsPending());

    uploader.run();

    assertTrue(uploader.onlyRefusedRequestsPending());

    EasyMock.verify(socket);
    EasyMock.verify(blockProvider);
    EasyMock.verify(throttler);
  }
}
