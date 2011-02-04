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

package ws.moor.bt.bencoding;

import ws.moor.bt.util.ExtendedTestCase;

import java.io.IOException;


public class BFormatterTest extends ExtendedTestCase {

  public void testDictionary() throws IOException, ParseException {
    String expected = "{\n" +
        "\tcomplete -> 1\n" +
        "\tincomplete -> 8\n" +
        "\tinterval -> 1800\n" +
        "\tmin interval -> 900\n" +
        "\tpeers -> Hallo\n" +
        "}";
    BEntity entity =
        new BDecoder().decode("d8:completei1e10:incompletei8e8:intervali1800e12:min intervali900e5:peers5:Halloe");
    assertEquals(expected, new BFormatter().prettyPrint(entity));
  }

  public void testListOfList() throws IOException, ParseException {
    String expected = "[\n" +
        "\t[\n" +
        "\t\t1a\n" +
        "\t\t1b\n" +
        "\t\t1c\n" +
        "\t]\n" +
        "\t[\n" +
        "\t\t2a\n" +
        "\t\t2b\n" +
        "\t\t2c\n" +
        "\t]\n" +
        "\t[\n" +
        "\t\t3a\n" +
        "\t\t3b\n" +
        "\t\t3c\n" +
        "\t]\n" +
        "]";
    BEntity entity = new BDecoder().decode("ll2:1a2:1b2:1cel2:2a2:2b2:2cel2:3a2:3b2:3cee");
    assertEquals(expected, new BFormatter().prettyPrint(entity));
  }
}
