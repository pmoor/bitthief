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

package ws.moor.bt.torrent;

import org.apache.log4j.Logger;
import ws.moor.bt.bencoding.BDecoder;
import ws.moor.bt.bencoding.BDictionary;
import ws.moor.bt.bencoding.BEntity;
import ws.moor.bt.bencoding.BInteger;
import ws.moor.bt.bencoding.BList;
import ws.moor.bt.bencoding.BString;
import ws.moor.bt.bencoding.ParseException;
import ws.moor.bt.util.LoggingUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * TODO(pmoor): Javadoc
 */
public class MetaInfoBuilder {

  private static Logger logger = LoggingUtil.getLogger(MetaInfoBuilder.class);

  public static final String COMMENT_FIELD       = "comment";
  public static final String ANNOUNCE_FIELD      = "announce";
  public static final String CREATION_DATE_FIELD = "creation date";
  public static final String PIECE_LENGTH_FIELD  = "piece length";
  public static final String NAME_FIELD          = "name";
  public static final String PIECES_FIELD        = "pieces";
  public static final String LENGTH_FIELD        = "length";
  public static final String PATH_FIELD          = "path";
  public static final String FILES_FIELD         = "files";
  public static final String INFO_FIELD          = "info";

  public static MetaInfo fromStream(InputStream inputStream) throws IOException, ParseException {
    BEntity entity = new BDecoder().decode(inputStream);
    if (!(entity instanceof BDictionary)) {
      throw new ParseException("root is not a dictionary entity");
    }
    return fromDictionary((BDictionary) entity);
  }

  public static MetaInfo fromFile(File file) throws IOException, ParseException {
    FileInputStream inputStream = new FileInputStream(file);
    MetaInfo metaInfo = fromStream(inputStream);
    inputStream.close();
    return metaInfo;
  }

  public static MetaInfo fromDictionary(BDictionary dictionary) throws ParseException {
    URL announceUrl = extractAnnounceUrl(dictionary);
    String comment = extractComment(dictionary);
    Date creationDate = extractCreationDate(dictionary);

    BEntity infoEntity = dictionary.getByString(INFO_FIELD);
    assertNotNull(infoEntity, INFO_FIELD);
    assertInstanceof(infoEntity, INFO_FIELD, BDictionary.class);
    BDictionary info = (BDictionary) infoEntity;

    Hash infoHash = calculateInfoHash(info);
    int pieceLength = extractPieceLength(info);
    String name = extractName(info);
    Hash[] pieceHashes = extractPieceHashes(info);

    if (isSingleFileTorrent(info)) {
      long length = extractLength(info);
      return new SingleFileMetaInfo(infoHash, announceUrl, comment, creationDate, pieceLength, name, pieceHashes, length);
    } else {
      MetaInfo.FileInfo[] fileInfos = extractFileInformations(info);
      return new MultiFileMetaInfo(infoHash, announceUrl, comment, creationDate, pieceLength, name, pieceHashes, fileInfos);
    }
  }

  private static MetaInfo.FileInfo[] extractFileInformations(BDictionary info) throws ParseException {
    BEntity filesEntity = info.getByString(FILES_FIELD);
    assertNotNull(filesEntity, FILES_FIELD);
    assertInstanceof(filesEntity, FILES_FIELD, BList.class);
    BList<BDictionary> files = (BList<BDictionary>) filesEntity;
    return extractFileInfos(files);
  }

  private static MetaInfo.FileInfo[] extractFileInfos(BList<BDictionary> files) throws ParseException {
    MetaInfo.FileInfo[] result = new MetaInfo.FileInfo[files.size()];
    int i = 0;
    for (BDictionary dictionary : files) {
      result[i++] = extractFileInfo(dictionary);
    }
    return result;
  }

  private static MetaInfo.FileInfo extractFileInfo(BDictionary dictionary) throws ParseException {
    String[] path = extractPath(dictionary);
    long length = extractLength(dictionary);
    return new MetaInfo.FileInfo(path, length);
  }

  private static boolean isSingleFileTorrent(BDictionary infoDictionary) throws ParseException {
    boolean hasLength = infoDictionary.getByString(LENGTH_FIELD) != null;
    boolean hasFiles = infoDictionary.getByString(FILES_FIELD) != null;
    if (hasLength && !hasFiles) {
      return true;
    } else if (!hasLength && hasFiles) {
      return false;
    } else {
      throw new ParseException("has either length and files field or none of them");
    }
  }

  private static String[] extractPath(BDictionary dictionary) throws ParseException {
    BEntity pathEntity = dictionary.getByString(PATH_FIELD);
    assertNotNull(pathEntity, PATH_FIELD);
    assertInstanceof(pathEntity, PATH_FIELD, BList.class);
    BList<BString> paths = (BList<BString>) pathEntity;
    String[] pathArray = new String[paths.size()];
    int i = 0;
    for (BString entry : paths) {
      pathArray[i++] = entry.toString();
    }
    return pathArray;
  }

  private static long extractLength(BDictionary info) throws ParseException {
    BEntity lengthEntity = info.getByString(LENGTH_FIELD);
    assertNotNull(lengthEntity, LENGTH_FIELD);
    assertInstanceof(lengthEntity, LENGTH_FIELD, BInteger.class);
    return ((BInteger) lengthEntity).longValue();
  }

  private static Hash[] extractPieceHashes(BDictionary info) throws ParseException {
    BEntity piecesEntity = info.getByString(PIECES_FIELD);
    assertNotNull(piecesEntity, PIECES_FIELD);
    assertInstanceof(piecesEntity, PIECES_FIELD, BString.class);
    return Hash.fromConcatenatedByteArrays(((BString) piecesEntity).getBytes());
  }

  private static String extractName(BDictionary info) throws ParseException {
    BEntity nameEntity = info.getByString(NAME_FIELD);
    assertNotNull(nameEntity, NAME_FIELD);
    assertInstanceof(nameEntity, NAME_FIELD, BString.class);
    return nameEntity.toString();
  }

  private static int extractPieceLength(BDictionary info) throws ParseException {
    BEntity lengthEntity = info.getByString(PIECE_LENGTH_FIELD);
    assertNotNull(lengthEntity, PIECE_LENGTH_FIELD);
    assertInstanceof(lengthEntity, PIECE_LENGTH_FIELD, BInteger.class);
    return ((BInteger) lengthEntity).intValue();
  }

  private static Hash calculateInfoHash(BDictionary info) throws ParseException {
    try {
      return Hash.forByteArray(info.encode());
    } catch (IOException e) {
      throw new ParseException("could not calculate info hash", e);
    }
  }

  private static Date extractCreationDate(BDictionary dictionary) throws ParseException {
    BEntity dateEntity = dictionary.getByString(CREATION_DATE_FIELD);
    if (dateEntity == null) {
      logger.warn("missing Creation Date in torrent info, continuing anyway");
      return null;
    }
    assertInstanceof(dateEntity, CREATION_DATE_FIELD, BInteger.class);
    long timestamp = ((BInteger) dateEntity).longValue();
    return new Date(1000 * timestamp);
  }

  private static String extractComment(BDictionary dictionary) throws ParseException {
    BEntity commentEntity = dictionary.getByString(COMMENT_FIELD);
    if (commentEntity == null) {
      // comment is optional
      return null;
    }
    assertInstanceof(commentEntity, COMMENT_FIELD, BString.class);
    return commentEntity.toString();
  }

  private static URL extractAnnounceUrl(BDictionary dictionary) throws ParseException {
    BEntity announceEntity = dictionary.getByString(ANNOUNCE_FIELD);
    assertNotNull(announceEntity, ANNOUNCE_FIELD);
    assertInstanceof(announceEntity, ANNOUNCE_FIELD, BString.class);
    try {
      return new URL(announceEntity.toString());
    } catch (MalformedURLException e) {
      throw new ParseException("could not parse announce url: " + announceEntity.toString(), e);
    }
  }

  private static void assertInstanceof(BEntity entity, String fieldName, Class<? extends BEntity> clazz)
      throws ParseException {
    if (!(clazz.isAssignableFrom(entity.getClass()))) {
      throw new ParseException("\"" + fieldName + "\" field is of wrong type");
    }
  }

  private static void assertNotNull(BEntity nameEntity, String fieldname) throws ParseException {
    if (nameEntity == null) {
      throw new ParseException("missing \"" + fieldname + "\" field");
    }
  }
}
