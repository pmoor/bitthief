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

import java.io.IOException;

public abstract class DHTMessage {

  private final TransactionId transactionId;
  private final String messageType;

  public static final char ERROR_MESSAGE_TYPE = 'e';
  public static final char QUERY_MESSAGE_TYPE = 'q';
  public static final char RESPONSE_MESSAGE_TYPE = 'r';

  protected DHTMessage(char messageType, TransactionId transactionId) {
    this.messageType = Character.toString(messageType);
    this.transactionId = transactionId;
  }

  public void populateDictionary(BDictionary<BEntity> dictionary) {
    dictionary.put(new BString("t"), new BString(transactionId.getBytes()));
    dictionary.put(new BString("y"), new BString(messageType));
  }

  public byte[] encode() throws IOException {
    BDictionary<BEntity> dictionary = new BDictionary<BEntity>();
    populateDictionary(dictionary);

    return dictionary.encode();
  }

  public TransactionId getTransactionId() {
    return transactionId;
  }
}
