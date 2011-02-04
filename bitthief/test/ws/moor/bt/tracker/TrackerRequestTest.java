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

package ws.moor.bt.tracker;

import ws.moor.bt.torrent.Hash;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.ExtendedTestCase;

/**
 * TODO(pmoor): Javadoc
 */
public class TrackerRequestTest extends ExtendedTestCase {

  Hash infoHash;
  PeerId peerId;
  int listeningPort;
  long bytesUploaded;
  long bytesDownloaded;
  long bytesLeft;
  private TrackerRequest.Event event;

  protected void setUp() throws Exception {
    super.setUp();

    byte[] tmp = new byte[20];
    byte[] tmp2 = new byte[20];
    for (byte i = 0; i < 20; i++) {
      tmp[i] = i;
      tmp2[i] = (byte) (19 - i);
    }
    infoHash = new Hash(tmp);
    peerId = new PeerId(tmp2);
    listeningPort = 42;
    bytesUploaded = 84 * 1024;
    bytesDownloaded = 22 * 1024;
    bytesLeft = 80 * 1024;
    event = TrackerRequest.Event.STARTED;
  }

  public void testCorrectConstruction() {
    TrackerRequest request = createStandardRequest();
    assertEquals(infoHash, request.getInfoHash());
    assertEquals(peerId, request.getPeerId());
    assertEquals(listeningPort, request.getListeningPort());
    assertEquals(event, request.getEvent());
    assertEquals(bytesUploaded, request.getUploaded());
    assertEquals(bytesDownloaded, request.getDownloaded());
    assertEquals(bytesLeft, request.getLeft());
    assertNull(request.getIPAddress());
    assertEquals(50, request.getPeersWanted());
  }

  public void testCorrectURLString() {
    TrackerRequest request = createStandardRequest();
    assertEquals("info_hash=%00%01%02%03%04%05%06%07%08%09%0a%0b%0c%0d%0e%0f%10%11%12%13" +
        "&peer_id=%13%12%11%10%0f%0e%0d%0c%0b%0a%09%08%07%06%05%04%03%02%01%00" +
        "&port=42&uploaded=86016&downloaded=22528&left=81920&event=started",
        request.getHttpEncodedParameters());
  }

  public void testCorrectExtendedURLString() {
    TrackerRequest request = createStandardRequest();
    request.setKey(ByteUtil.newByteArray(0xff, 0x77, 0x99, 0x77));
    request.setCompactFormatWanted(true);
    assertEquals("info_hash=%00%01%02%03%04%05%06%07%08%09%0a%0b%0c%0d%0e%0f%10%11%12%13" +
        "&peer_id=%13%12%11%10%0f%0e%0d%0c%0b%0a%09%08%07%06%05%04%03%02%01%00" +
        "&port=42&uploaded=86016&downloaded=22528&left=81920&event=started" +
        "&compact=1&key=%ffw%99w",
        request.getHttpEncodedExtendedParameters());
  }

  public void testCorrectExtendedURLStringNoEvent() {
    event = null;
    TrackerRequest request = createStandardRequest();
    request.setKey(ByteUtil.newByteArray(0xff, 0x77, 0x99, 0x77));
    request.setCompactFormatWanted(true);
    assertEquals("info_hash=%00%01%02%03%04%05%06%07%08%09%0a%0b%0c%0d%0e%0f%10%11%12%13" +
        "&peer_id=%13%12%11%10%0f%0e%0d%0c%0b%0a%09%08%07%06%05%04%03%02%01%00" +
        "&port=42&uploaded=86016&downloaded=22528&left=81920&compact=1" +
        "&key=%ffw%99w",
        request.getHttpEncodedExtendedParameters());
  }

  public void testNullId() {
    peerId = null;
    shouldFailNow();
  }

  public void testNullEvent() {
    event = null;
    shouldNotFailNow();
  }

  public void testNullHash() {
    infoHash = null;
    shouldFailNow();
  }

  public void testInvalidPort() {
    listeningPort = 0;
    shouldFailNow();
    listeningPort = -5;
    shouldFailNow();
    listeningPort = 70000;
    shouldFailNow();
  }

  public void testInvalidUpload() {
    bytesUploaded = -1;
    shouldFailNow();
  }

  public void testInvalidDownload() {
    bytesDownloaded = -1;
    shouldFailNow();
  }

  public void testInvalidLeft() {
    bytesLeft = -1;
    shouldFailNow();
  }


  private void shouldFailNow() {
    try {
      createStandardRequest();
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
      // expected
    } catch (NullPointerException npe) {
      // expected
    }
  }

  private void shouldNotFailNow() {
    try {
      createStandardRequest();
      //expected
    } catch (IllegalArgumentException e) {
      fail("IllegalArgumentException should not be thrown");
    }
  }

  private TrackerRequest createStandardRequest() {
    return new TrackerRequest(
        infoHash,
        peerId,
        listeningPort,
        event,
        bytesUploaded,
        bytesDownloaded,
        bytesLeft);
  }
}
