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

import org.apache.log4j.Logger;
import ws.moor.bt.downloader.Block;
import ws.moor.bt.stats.CounterRepository;
import ws.moor.bt.storage.DataBlock;
import ws.moor.bt.storage.DataBlockProvider;
import ws.moor.bt.util.LoggingUtil;
import ws.moor.bt.util.PrefixLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class Uploader implements Runnable {

  private final CostThrottler throttler;
  private final PacketSocket socket;
  private final DataBlockProvider blockProvider;

  private final List<DataBlock> requestQueue = new ArrayList<DataBlock>();
  private final List<Block> refusedRequestsQueue = new ArrayList<Block>();

  private final CounterRepository counterRepository;

  private static final Logger logger = LoggingUtil.getLogger(Uploader.class);
  private final PrefixLogger prefixLogger;

  private static final int MAX_REQUEST_QUEUE_SIZE = 16;
  private static final int MAX_BLOCK_LENGTH = 32768;

  public Uploader(CostThrottler throttler,
                  PacketSocket socket,
                  DataBlockProvider blockProvider,
                  CounterRepository counterRepository) {
    this.throttler = throttler;
    this.socket = socket;
    this.blockProvider = blockProvider;
    this.counterRepository = counterRepository;

    prefixLogger = new PrefixLogger(logger, socket.getInstrumentationKey() + " ");
  }

  public synchronized void addRequest(Block block) {
    prefixLogger.trace("adding a request for block " + block);
    if (requestQueue.size() >= MAX_REQUEST_QUEUE_SIZE) {
      prefixLogger.debug("request queue is too big, dropping the block");
      return;
    } else if (block.getLength() > MAX_BLOCK_LENGTH) {
      prefixLogger.debug("requested block length is too big: " + block.getLength());
      return;
    }

    DataBlock dataBlock = fetchDataForBlock(block);
    if (dataBlock == null) {
      return;
    }

    requestQueue.add(dataBlock);
    updateRequestQueueStats();
    reserveSpot();
  }

  public synchronized void cancelRequest(Block block) {
    Iterator<DataBlock> iterator = requestQueue.iterator();
    while (iterator.hasNext()) {
      DataBlock ourBlock = iterator.next();
      if (ourBlock.getPieceIndex() == block.getPieceIndex() &&
          ourBlock.getOffset() == block.getOffset() &&
          ourBlock.getLength() == block.getLength()) {
        iterator.remove();
        return;
      }
    }
    updateRequestQueueStats();
  }

  private DataBlock fetchDataForBlock(Block block) {
    try {
      return blockProvider.getBlock(block);
    } catch (IllegalArgumentException e) {
      prefixLogger.debug("could not fetch data for block " + block, e);
      return null;
    } catch (DataBlockProvider.BlockRefusedException e) {
      counterRepository.getCounter("torrent.request.refusing", socket.getInstrumentationKey()).increase(1);
      prefixLogger.debug("refused to share " + block);
      refusedRequestsQueue.add(block);
      return null;
    }
  }

  private void reserveSpot() {
    if (requestQueue.isEmpty()) {
      throttler.removeJob(this);
      return;
    }
    Block block = requestQueue.get(0);
    throttler.submitJob(block.getLength(), this);
  }

  public synchronized void flush() {
    requestQueue.clear();
    refusedRequestsQueue.clear();
    updateRequestQueueStats();
  }

  public void run() {
    prefixLogger.debug("we're good to go!");

    DataBlock block = removeFirstBlockFromQueue();
    updateRequestQueueStats();

    try {
      if (block != null) {
        prefixLogger.debug("sending piece packet for block " + block);
        socket.sendPacket(block.toPiecePacket());
        counterRepository.getCounter("network.piece.out", socket.getInstrumentationKey()).increase(1);
      }
    } catch (IOException e) {
      prefixLogger.warn("exception while sending a piece packet", e);
    }

    reserveSpot();
  }

  private synchronized DataBlock removeFirstBlockFromQueue() {
    if (requestQueue.isEmpty()) {
      return null;
    }
    return requestQueue.remove(0);
  }

  private void updateRequestQueueStats() {
    counterRepository.getCounter("network.requestqueue", socket.getInstrumentationKey()).set(requestQueue.size());
  }

  public boolean onlyRefusedRequestsPending() {
    return !refusedRequestsQueue.isEmpty() && requestQueue.isEmpty();
  }
}
