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

package ws.moor.bt.dht.messages;

import ws.moor.bt.bencoding.BDictionary;
import ws.moor.bt.bencoding.BEntity;
import ws.moor.bt.bencoding.BList;
import ws.moor.bt.bencoding.BString;
import ws.moor.bt.dht.Token;
import ws.moor.bt.dht.TransactionId;
import ws.moor.bt.torrent.PeerId;
import ws.moor.bt.tracker.TrackerResponse;
import ws.moor.bt.util.ArrayUtil;

public class ResponseMessage extends DHTMessage {

  private final BDictionary<BEntity> returnValues;

  private ResponseMessage(TransactionId transactionId, BDictionary<BEntity> returnValues) {
    super(RESPONSE_MESSAGE_TYPE, transactionId);
    this.returnValues = returnValues;
  }

  public void populateDictionary(BDictionary<BEntity> dictionary) {
    dictionary.put(new BString("r"), returnValues);
    super.populateDictionary(dictionary);
  }

  public static ResponseMessage constructPingReply(TransactionId transactionId, PeerId id) {
    BDictionary<BEntity> values = new BDictionary<BEntity>();
    values.put(new BString("id"), new BString(id.getBytes()));
    return new ResponseMessage(transactionId, values);
  }

  public static ResponseMessage constructGetPeersReply(TransactionId transactionId,
                                                       PeerId id,
                                                       Token token,
                                                       TrackerResponse.PeerInfo[] peerInfo) {
    BDictionary<BEntity> values = new BDictionary<BEntity>();
    values.put(new BString("id"), new BString(id.getBytes()));
    BList<BEntity> list = new BList<BEntity>();
    for (TrackerResponse.PeerInfo peer : peerInfo) {
      list.add(new BString(peer.toCompactForm()));
    }
    values.put(new BString("values"), list);
    values.put(new BString("token"), new BString(token.getBytes()));
    return new ResponseMessage(transactionId, values);
  }

  public static ResponseMessage constructGetPeersReplyNoPeers(TransactionId transactionId,
                                                              PeerId id,
                                                              Token token,
                                                              TrackerResponse.PeerInfo[] nodes) {
    BDictionary<BEntity> values = new BDictionary<BEntity>();
    values.put(new BString("id"), new BString(id.getBytes()));
    byte[] nodeData = new byte[0];
    for (TrackerResponse.PeerInfo node : nodes) {
      nodeData = ArrayUtil.append(nodeData, node.toCompactLongForm());
    }
    values.put(new BString("nodes"), new BString(nodeData));
    values.put(new BString("token"), new BString(token.getBytes()));
    return new ResponseMessage(transactionId, values);
  }

  public static ResponseMessage constructFindNodesReply(TransactionId transactionId,
                                                        PeerId id,
                                                        TrackerResponse.PeerInfo[] nodes) {
    BDictionary<BEntity> values = new BDictionary<BEntity>();
    values.put(new BString("id"), new BString(id.getBytes()));
    byte[] nodeData = new byte[0];
    for (TrackerResponse.PeerInfo node : nodes) {
      nodeData = ArrayUtil.append(nodeData, node.toCompactLongForm());
    }
    values.put(new BString("nodes"), new BString(nodeData));
    return new ResponseMessage(transactionId, values);
  }
}
