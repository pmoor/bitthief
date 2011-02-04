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

import ws.moor.bt.torrent.MetaInfo;

import java.io.File;

public class TorrentDownloadConfiguration {

  private final MetaInfo metaInfo;
  private final File targetDirectory;
  private int listeningPort = DEFAULT_LISTENING_PORT;
  private boolean downloadFromSeeders = true;
  private boolean initiateConnections = true;
  private boolean sendRealBitField = false;
  private boolean showGui = false;
  private int initialAnnounceInterval = DEFAULT_ANNOUNCE_INTERVAL;
  private double shareRatio = DEFAULT_SHARE_RATIO;
  private boolean quitOnFinish = false;
  private int maxUploadRate = DEFAULT_MAX_UPLOAD_RATE;
  private boolean uploading = DEFAULT_UPLOADING_STATUS;
  private int uploadSlots = DEFAULT_UPLOAD_SLOTS;
  private int announcePercent = DEFAULT_BITFIELD_PERCENT;
  private boolean uploadRealData = DEFAULT_UPLOAD_REAL_DATA;
  private int pieceUploadDenyPercentage = DEFAULT_PIECE_UPLOAD_DENY_PERCENTAGE;

  public static final int DEFAULT_LISTENING_PORT = 6811;
  public static final int DEFAULT_ANNOUNCE_INTERVAL = 180;
  public static final double DEFAULT_SHARE_RATIO = 1.092;
  public static final int DEFAULT_MAX_UPLOAD_RATE = 32;
  public static final boolean DEFAULT_UPLOADING_STATUS = false;
  public static final int DEFAULT_UPLOAD_SLOTS = 4;
  public static final int DEFAULT_BITFIELD_PERCENT = 0;
  public static final boolean DEFAULT_UPLOAD_REAL_DATA = false;
  public static final int DEFAULT_PIECE_UPLOAD_DENY_PERCENTAGE = 1;

  public TorrentDownloadConfiguration(MetaInfo metaInfo, File targetDirectory) {
    this.metaInfo = metaInfo;
    this.targetDirectory = targetDirectory;
  }

  public MetaInfo getMetaInfo() {
    return metaInfo;
  }

  public File getTargetDirectory() {
    return targetDirectory;
  }

  public int getListeningPort() {
    return listeningPort;
  }

  public void setListeningPort(int listeningPort) {
    this.listeningPort = listeningPort;
  }

  public void setDownloadFromSeeders(boolean downloadFromSeeders) {
    this.downloadFromSeeders = downloadFromSeeders;
  }

  public boolean isDownloadingFromSeeders() {
    return downloadFromSeeders;
  }

  public boolean isInitiatingConnections() {
    return initiateConnections;
  }

  public void setInitiateConnections(boolean initiateConnections) {
    this.initiateConnections = initiateConnections;
  }

  public boolean sendRealBitField() {
    return sendRealBitField;
  }

  public void setSendRealBitField(boolean sendRealBitField) {
    this.sendRealBitField = sendRealBitField;
  }

  public void setShowGui(boolean showGui) {
    this.showGui = showGui;
  }

  public boolean showGui() {
    return showGui;
  }

  public long getInitialAnnounceInterval() {
    return initialAnnounceInterval * 1000;
  }

  public void setInitialAnnounceInterval(int initialAnnounceInterval) {
    this.initialAnnounceInterval = initialAnnounceInterval;
  }

  public double getShareRatio() {
    return shareRatio;
  }

  public void setShareRatio(double shareRatio) {
    this.shareRatio = shareRatio;
  }

  public void setQuitOnFinish(boolean quitOnFinish) {
    this.quitOnFinish = quitOnFinish;
  }

  public boolean doQuitOnFinish() {
    return quitOnFinish;
  }

  public void setMaxUploadRate(int maxUploadRate) {
    this.maxUploadRate = maxUploadRate;
  }

  public int getMaxUploadRate() {
    return maxUploadRate;
  }

  public boolean isUploading() {
    return uploading;
  }

  public void setUploading(boolean doUpload) {
    this.uploading = doUpload;
  }

  public int getUploadSlots() {
    return uploadSlots;
  }

  public void setUploadSlots(int uploadSlots) {
    this.uploadSlots = uploadSlots;
  }

  public int getAnnouncePercent() {
    return announcePercent;
  }

  public void setBitfieldPercent(int announcePercent) {
    this.announcePercent = announcePercent;
  }

  public boolean doUploadRealData() {
    return uploadRealData;
  }

  public void setUploadRealData(boolean uploadRealData) {
    this.uploadRealData = uploadRealData;
  }

  public int getPieceUploadDenyPercentage() {
    return pieceUploadDenyPercentage;
  }

  public void setPieceUploadDenyPercentage(int percentage) {
    pieceUploadDenyPercentage = percentage;
  }
}
