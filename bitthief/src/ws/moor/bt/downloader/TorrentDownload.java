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

package ws.moor.bt.downloader;

import org.apache.log4j.Logger;
import ws.moor.bt.Environment;
import ws.moor.bt.downloader.peermanager.PeerManager;
import ws.moor.bt.et.ET;
import ws.moor.bt.network.BitTorrentConnection;
import ws.moor.bt.network.BitTorrentListener;
import ws.moor.bt.network.ConnectionInitiator;
import ws.moor.bt.network.CostThrottler;
import ws.moor.bt.stats.CounterRepository;
import ws.moor.bt.stats.RealCounterRepository;
import ws.moor.bt.storage.BitField;
import ws.moor.bt.storage.DataBlockProvider;
import ws.moor.bt.storage.PieceManager;
import ws.moor.bt.storage.RandomDataBlockProvider;
import ws.moor.bt.storage.Storage;
import ws.moor.bt.storage.StorageBuilder;
import ws.moor.bt.torrent.Hash;
import ws.moor.bt.torrent.MetaInfo;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.tracker.TrackerClient;
import ws.moor.bt.util.LoggingUtil;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * TODO(pmoor): Javadoc
 */
public class TorrentDownload {

  private final Environment environment;
  private final TorrentDownloadConfiguration configuration;

  private final Storage storage;
  private final PieceManager pieceManager;
  private final NextBlockStrategy nextBlockStrategy;
  private final PeerId trackerRegisteredPeerId;
  private final PeerManager peerManager;
  private final TrackerClient trackerClient;
  private final CostThrottler costThrottler;
  private final ConnectionInitiator connectionInitiator;
  private final Choker choker;
  private final ET et;

  private CounterRepository counterRepository;

  private boolean running = false;
  private static final Logger logger = LoggingUtil.getLogger(TorrentDownload.class);

  public TorrentDownload(Environment environment,
                         TorrentDownloadConfiguration configuration) {
    this.environment = environment;
    this.configuration = configuration;

    MetaInfo metaInfo = configuration.getMetaInfo();
    logger.info("creating torrent download for \"" + metaInfo.getName() + "\"");
    storage = new StorageBuilder().buildStorage(metaInfo, configuration.getTargetDirectory());
    pieceManager = new PieceManager(metaInfo, storage, environment, getCounterRepository());
    nextBlockStrategy = new DummyNextBlockStrategy(metaInfo, pieceManager);
    peerManager = new PeerManager(getCounterRepository());
    connectionInitiator = createConnectionInitiator();
    trackerClient = new TrackerClient(getMetaInfo().getAnnounceUrl(), peerManager, this);
    costThrottler = new CostThrottler(configuration.getMaxUploadRate() * 1000, 1000, getCounterRepository());
    choker = createChoker();
    et = new ET(environment.getConfiguration());

    trackerRegisteredPeerId = PeerId.createRandomMainlineId();
    environment.getDownloadRepository().addDownload(this);
  }

  private Choker createChoker() {
    if (configuration.isUploading()) {
      return new Choker(this, configuration.getUploadSlots());
    }
    return null;
  }

  private ConnectionInitiator createConnectionInitiator() {
    if (configuration.isInitiatingConnections()) {
      return new ConnectionInitiator(this);
    } else {
      return null;
    }
  }

  public synchronized void start() {
    assertNotRunning();
    running = true;
    environment.getScheduledExecutor().scheduleWithFixedDelay(
        new Maintenance(), 0, 15, TimeUnit.SECONDS);
    costThrottler.start();
    et.callHomeForStart(this);
  }

  private void assertNotRunning() {
    if (running) {
      throw new IllegalStateException("should not be running");
    }
  }

  public synchronized void stop() {
    assertRunning();
    running = false;
    costThrottler.stop();
    environment.getDownloadRepository().removeDownload(this);
    environment.getConnectionRepository().closeAllConnections(this);
    et.callHomeForStop(this);
  }

  private void assertRunning() {
    if (!running) {
      throw new IllegalStateException("should be running");
    }
  }

  public BitTorrentListener getDefaultListener() {
    try {
      return environment.getListener(configuration.getListeningPort());
    } catch (IOException e) {
      logger.fatal("could not obtain a listener", e);
    }
    return null;
  }

  public PeerId getTrackerRegisteredPeerId() {
    return trackerRegisteredPeerId;
  }

  public MetaInfo getMetaInfo() {
    return configuration.getMetaInfo();
  }

  public NextBlockStrategy getNextBlockStrategy() {
    return nextBlockStrategy;
  }

  public PieceManager getPieceManager() {
    return pieceManager;
  }

  public Environment getEnvironment() {
    return environment;
  }

  public long getBytesUploaded() {
    return (long) (getBytesDownloaded() * configuration.getShareRatio());
  }

  public long getBytesDownloaded() {
    int availablePieceCount =
        pieceManager.getValidPieces().getAvailablePieceCount();
    return availablePieceCount * getMetaInfo().getPieceLength();
  }

  public long getBytesLeft() {
    return (long) (getMetaInfo().getTotalLength() - getBytesDownloaded() * 0.9);
  }

  public Set<BitTorrentConnection> getAllValidConnections() {
    return environment.getConnectionRepository().getAllValidConnections(this);
  }

  public TorrentDownloadConfiguration getConfiguration() {
    return configuration;
  }

  public Hash getInfoHash() {
    return getMetaInfo().getInfoHash();
  }

  public int getPort() {
    return getDefaultListener().getPort();
  }

  public PieceAnnounceStrategy getPieceAnnounceStrategy(BitTorrentConnection connection) {
    if (configuration.sendRealBitField()) {
      return new RealPieceAnnounceStrategy(this);
    } else {
      return new RandomPieceAnnounceStrategy(this,
          connection, configuration.getAnnouncePercent());
    }
  }

  private boolean isFinished() {
    return pieceManager.getValidPieces().hasAll();
  }

  public CostThrottler getThrottler() {
    return costThrottler;
  }

  public PeerManager getPeerManager() {
    return peerManager;
  }

  public DataBlockProvider getBlockProvider() {
    if (configuration.doUploadRealData()) {
      return pieceManager;
    } else {
      int numberOfBlocksToDeny = (int) Math.ceil(
          (double) storage.getPieceLength() * configuration.getPieceUploadDenyPercentage()
          / (16384 * 100));
      return new RandomDataBlockProvider(
          storage.getPieceCount(), storage.getPieceLength(), storage.getLastPieceLength(),
          numberOfBlocksToDeny);
    }
  }

  public CounterRepository getCounterRepository() {
    if (counterRepository == null) {
      createCounterRepository();
    }
    return counterRepository;
  }

  private synchronized void createCounterRepository() {
    if (counterRepository == null) {
      RealCounterRepository repo =
          RealCounterRepository.fromResource("stats.properties");
      if (environment.getConfiguration().doLogStats()) {
        repo.setFileForWriting(environment.getConfiguration().getStatsLogFile());
      }
      counterRepository = repo;
    }
  }

  private class Maintenance implements Runnable {
    public void run() {
      logger.info("torrent download maintenance");
      if (!running) {
        logger.info("we're not running anymore, killing the maintenance job");
        throw new IllegalStateException("quitting now");
      }
      trackerClient.announce();
      if (connectionInitiator != null) {
        connectionInitiator.maintenance();
      }
      if (choker != null) {
        choker.maintenance();
      }
      updateConnectionStatistics();
      quitOnFinish();
    }

    private void quitOnFinish() {
      if (configuration.doQuitOnFinish() && isFinished()) {
        logger.info("exiting, finished download");
        System.exit(0);
      }
    }
  }

  private void updateConnectionStatistics() {
    int seeds = 0;
    int leechers = 0;
    int in = 0;
    int out = 0;
    Set<BitTorrentConnection> connections = getAllValidConnections();
    for (BitTorrentConnection connection : connections) {
      BitField bitField = connection.getRemoteBitField();
      if (bitField != null) {
        if (bitField.hasAll()) {
          seeds++;
        } else {
          leechers++;
        }
      }

      if (connection.isInbound()) {
        in++;
      } else {
        out++;
      }
    }

    getCounterRepository().getCounter("network.connections.seed").set(seeds);
    getCounterRepository().getCounter("network.connections.leecher").set(leechers);
    getCounterRepository().getCounter("network.connections.in").set(in);
    getCounterRepository().getCounter("network.connections.out").set(out);
  }
}
