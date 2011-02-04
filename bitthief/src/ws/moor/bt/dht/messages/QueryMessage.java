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
import ws.moor.bt.bencoding.BString;
import ws.moor.bt.dht.TransactionId;
import ws.moor.bt.torrent.PeerId;

public class QueryMessage extends DHTMessage {

  private final String queryType;

  private final BDictionary<BEntity> arguments;

  public static final String PING_QUERY = "ping";
  public static final String FIND_NODE_QUERY = "find_node";
  public static final String GET_PEERS_QUERY = "get_peers";
  public static final String ANNOUNCE_PEER_QUERY = "announce_peer";

  private QueryMessage(TransactionId transactionId, String queryType, BDictionary<BEntity> arguments) {
    super(QUERY_MESSAGE_TYPE, transactionId);
    this.queryType = queryType;
    this.arguments = arguments;
  }

  public void populateDictionary(BDictionary<BEntity> dictionary) {
    dictionary.put(new BString("a"), arguments);
    dictionary.put(new BString("q"), new BString(queryType));
    super.populateDictionary(dictionary);
  }

  public static QueryMessage constructPingQuery(TransactionId transactionId, PeerId id) {
    BDictionary<BEntity> args = new BDictionary<BEntity>();
    args.put(new BString("id"), new BString(id.getBytes()));
    return new QueryMessage(transactionId, PING_QUERY, args);
  }

  public static QueryMessage constructFindNodeQuery(TransactionId transactionId, PeerId id, PeerId target) {
    BDictionary<BEntity> args = new BDictionary<BEntity>();
    args.put(new BString("id"), new BString(id.getBytes()));
    args.put(new BString("target"), new BString(target.getBytes()));
    return new QueryMessage(transactionId, FIND_NODE_QUERY, args);
  }

  public String getQueryType() {
    return queryType;
  }
}
