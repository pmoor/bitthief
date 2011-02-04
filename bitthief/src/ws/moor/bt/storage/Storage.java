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

package ws.moor.bt.storage;

import ws.moor.bt.torrent.MetaInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO(pmoor): Javadoc
 */
public class Storage {

  private final int pieceSize;
  private final long totalSize;
  private final FileData fileData[];

  private FileRepository fileRepository;

  public Storage(FileRepository fileRepository, File parent,
                 MetaInfo.FileInfo[] fileInfos, int pieceSize) {
    this.pieceSize = pieceSize;
    this.fileRepository = fileRepository;
    fileData = createFileData(parent, fileInfos);
    totalSize = calculateTotalSize(fileInfos);
  }

  private long calculateTotalSize(MetaInfo.FileInfo[] infos) {
    long sum = 0;
    for (MetaInfo.FileInfo info : infos) {
      sum += info.getLength();
    }
    return sum;
  }

  private FileData[] createFileData(File parent, MetaInfo.FileInfo[] infos) {
    long globalOffset = 0;
    int index = 0;
    FileData[] result = new FileData[infos.length];
    for (MetaInfo.FileInfo info : infos) {
      result[index] = new FileData(info.constructFile(parent), globalOffset, info.getLength());
      globalOffset += info.getLength();
      index++;
    }
    return result;
  }

  public int writePiece(long pieceIndex, byte[] bytes)
      throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
    assertBufferNotNull(bytes);
    assertValidPieceIndex(pieceIndex);
    assertBufferLargeEnough(pieceIndex, bytes);
    List<PieceFraction> fractions = calculatePieceFractions(pieceIndex);
    return writeFractions(fractions, bytes);
  }

  public int readPiece(long pieceIndex, byte[] bytes)
      throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
    assertBufferNotNull(bytes);
    assertValidPieceIndex(pieceIndex);
    assertBufferLargeEnough(pieceIndex, bytes);
    List<PieceFraction> fractions = calculatePieceFractions(pieceIndex);
    return readFractions(fractions, bytes);
  }

  private void assertBufferNotNull(byte[] bytes) throws NullPointerException {
    if (bytes == null) {
      throw new NullPointerException("buffer is null");
    }
  }

  private void assertBufferLargeEnough(long pieceIndex, byte[] bytes) throws IllegalArgumentException {
    int expectedSize = getPieceLength(pieceIndex);
    if (bytes.length < expectedSize) {
      throw new IllegalArgumentException(
          "buffer is too small: " + bytes.length + " instead of " + expectedSize);
    }
  }

  private void assertValidPieceIndex(long pieceIndex) throws IndexOutOfBoundsException {
    if (pieceIndex < 0 || pieceIndex >= getPieceCount()) {
      throw new IndexOutOfBoundsException(
          "piece index (" + pieceIndex + ") does not fit into [0," + getPieceCount() + ")");
    }
  }

  public int getPieceCount() {
    return (int) Math.ceil((double) totalSize / pieceSize);
  }

  public int getPieceLength() {
    return pieceSize;
  }

  public int getLastPieceLength() {
    return (int) ((totalSize - 1) % pieceSize) + 1;
  }

  public int getPieceLength(long index) {
    assertValidPieceIndex(index);
    if (index == getPieceCount() - 1) {
      return getLastPieceLength();
    }
    return getPieceLength();
  }

  private int writeFractions(List<PieceFraction> fractions, byte[] bytes) {
    int offset = 0;
    for (PieceFraction fraction : fractions) {
      fileRepository.write(bytes, offset, fraction.file, fraction.offset, fraction.size);
      offset += fraction.size;
    }
    return offset;
  }

  private int readFractions(List<PieceFraction> fractions, byte[] bytes) {
    int offset = 0;
    for (PieceFraction fraction : fractions) {
      fileRepository.read(fraction.file, fraction.offset, bytes, offset, fraction.size);
      offset += fraction.size;
    }
    return offset;
  }

  private List<PieceFraction> calculatePieceFractions(long pieceIndex) {
    long startOffset = pieceSize * pieceIndex;
    long endOffset = Math.min(totalSize, startOffset + pieceSize);

    List<PieceFraction> pieceFractions = new ArrayList<PieceFraction>();
    while (startOffset < endOffset) {
      int fileIndex = findFileThatContainsOffset(startOffset);
      long offsetInFile = startOffset - fileData[fileIndex].globalOffset;
      long sizeInFile = Math.min(endOffset - startOffset, fileData[fileIndex].length - offsetInFile);
      pieceFractions.add(new PieceFraction(fileData[fileIndex].file, offsetInFile, sizeInFile));
      startOffset += sizeInFile;
    }

    return pieceFractions;
  }

  private int findFileThatContainsOffset(long startOffset) {
    for (int i = 0; i < fileData.length; i++) {
      if (fileData[i].globalOffset <= startOffset && startOffset < fileData[i].globalOffset + fileData[i].length) {
        return i;
      }
    }
    throw new IndexOutOfBoundsException("offset is too big, was " + startOffset + " out of " + totalSize + " allowed");
  }

  private class FileData {
    final File file;
    final long globalOffset;
    final long length;

    public FileData(File file, long globalOffset, long length) {
      this.file = file;
      this.globalOffset = globalOffset;
      this.length = length;
    }
  }

  private class PieceFraction {
    final File file;
    final long offset;
    final long size;

    public PieceFraction(File file, long offset, long size) {
      this.file = file;
      this.offset = offset;
      this.size = size;
    }
  }
}
