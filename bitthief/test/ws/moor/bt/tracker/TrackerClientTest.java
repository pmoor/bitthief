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

import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;
import ws.moor.bt.downloader.TorrentDownload;
import ws.moor.bt.downloader.TorrentDownloadConfiguration;
import ws.moor.bt.downloader.peermanager.PeerManager;
import ws.moor.bt.stats.FakeRepository;
import ws.moor.bt.torrent.Hash;
import ws.moor.bt.torrent.HashTest;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.util.ByteUtil;
import ws.moor.bt.util.ExtendedTestCase;
import ws.moor.bt.util.URLFetcher;
import ws.moor.bt.util.URLFetcherFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

/**
 * TODO(pmoor): Javadoc
 */
public class TrackerClientTest extends ExtendedTestCase {

  public void testBasicRequest() throws IOException {
    Hash hash = HashTest.randomHash();
    PeerId peerId = PeerId.createRandomMainlineId();

    URLFetcher fetcher = createMock(URLFetcher.class);
    String result = "d8:completei42e10:incompletei17e8:intervali1800e5:peerslee";
    expect(fetcher.fetch()).andReturn(result.getBytes());
    replay(fetcher);

    URLFetcherFactory factory = createMock(URLFetcherFactory.class);
    expect(factory.createURLFetcher(eq(new URL(
        "http://moor.ws/announce?info_hash=" + ByteUtil.urlEncode(hash.getBytes()) +
            "&peer_id=" + ByteUtil.urlEncode(peerId.getBytes()) + "&port=8080&uploaded=42" +
            "&downloaded=28&left=100&event=started&numwant=200&compact=1&key=%ffB")))).andReturn(fetcher);
    replay(factory);

    PeerManager peerManager = createMock(PeerManager.class);
    peerManager.addPeersFromTrackerResponse((Collection<TrackerResponse.PeerInfo>) EasyMock.anyObject());
    replay(peerManager);

    TorrentDownload download = createMock(TorrentDownload.class);
    expect(download.getInfoHash()).andReturn(hash);
    expect(download.getTrackerRegisteredPeerId()).andReturn(peerId);
    expect(download.getPort()).andReturn(8080);
    expect(download.getBytesUploaded()).andReturn(42l);
    expect(download.getBytesDownloaded()).andReturn(28l);
    expect(download.getBytesLeft()).andReturn(100l);
    expect(download.getConfiguration()).andReturn(new TorrentDownloadConfiguration(null, null));
    expect(download.getCounterRepository()).andReturn(new FakeRepository()).anyTimes();
    replay(download);

    TrackerClient client = new TrackerClient(new URL("http://moor.ws/announce"), peerManager, download);
    client.setKey(ByteUtil.newByteArray(0xff, 0x42));
    client.setURLFetcherFactory(factory);
    client.announce();

    verify(download);
    verify(peerManager);
    verify(factory);
  }
}
