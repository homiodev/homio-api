package org.homio.api.fs.archive.tar;

import java.io.File;

public class TarHeader {

  /*
   * Header
   */
  public static final int NAMELEN = 100;
  public static final int MODELEN = 8;
  public static final int UIDLEN = 8;
  public static final int GIDLEN = 8;
  public static final int SIZELEN = 12;
  public static final int MODTIMELEN = 12;
  public static final int CHKSUMLEN = 8;
  public static final byte LF_OLDNORM = 0;

  /*
   * File Types
   */
  public static final byte LF_NORMAL = (byte) '0';
  public static final byte LF_LINK = (byte) '1';
  public static final byte LF_SYMLINK = (byte) '2';
  public static final byte LF_CHR = (byte) '3';
  public static final byte LF_BLK = (byte) '4';
  public static final byte LF_DIR = (byte) '5';
  public static final byte LF_FIFO = (byte) '6';
  public static final byte LF_CONTIG = (byte) '7';

  /*
   * Ustar header
   */
  public static final String USTAR_MAGIC = "ustar"; // POSIX

  public static final int USTAR_MAGICLEN = 8;
  public static final int USTAR_USER_NAMELEN = 32;
  public static final int USTAR_GROUP_NAMELEN = 32;
  public static final int USTAR_DEVLEN = 8;
  public static final int USTAR_FILENAME_PREFIX = 155;

  // Header values
  public StringBuffer name;
  public int mode;
  public int userId;
  public int groupId;
  public long size;
  public long modTime;
  public int checkSum;
  public byte linkFlag;
  public StringBuffer linkName;
  public StringBuffer magic; // ustar indicator and version
  public StringBuffer userName;
  public StringBuffer groupName;
  public int devMajor;
  public int devMinor;
  public StringBuffer namePrefix;

  public TarHeader() {
    magic = new StringBuffer(TarHeader.USTAR_MAGIC);

    name = new StringBuffer();
    linkName = new StringBuffer();

    String user = System.getProperty("user.name", "");

    if (user.length() > 31) {
      user = user.substring(0, 31);
    }

    userId = 0;
    groupId = 0;
    userName = new StringBuffer(user);
    groupName = new StringBuffer();
    namePrefix = new StringBuffer();
  }

  public static StringBuffer parseName(byte[] header, int offset, int length) {
    StringBuffer result = new StringBuffer(length);

    int end = offset + length;
    for (int i = offset; i < end; ++i) {
      if (header[i] == 0) {
        break;
      }
      result.append((char) header[i]);
    }

    return result;
  }

  public static int getNameBytes(StringBuffer name, byte[] buf, int offset, int length) {
    int i;

    for (i = 0; i < length && i < name.length(); ++i) {
      buf[offset + i] = (byte) name.charAt(i);
    }

    for (; i < length; ++i) {
      buf[offset + i] = 0;
    }

    return offset + length;
  }

  public static TarHeader createHeader(String entryName, long size, long modTime, boolean dir) {
    String name = entryName;
    name = TarUtils.trim(name.replace(File.separatorChar, '/'), '/');

    TarHeader header = new TarHeader();
    header.linkName = new StringBuffer();

    if (name.length() > 100) {
      header.namePrefix = new StringBuffer(name.substring(0, name.lastIndexOf('/')));
      header.name = new StringBuffer(name.substring(name.lastIndexOf('/') + 1));
    } else {
      header.name = new StringBuffer(name);
    }

    if (dir) {
      header.mode = 040755;
      header.linkFlag = TarHeader.LF_DIR;
      if (header.name.charAt(header.name.length() - 1) != '/') {
        header.name.append("/");
      }
      header.size = 0;
    } else {
      header.mode = 0100644;
      header.linkFlag = TarHeader.LF_NORMAL;
      header.size = size;
    }

    header.modTime = modTime;
    header.checkSum = 0;
    header.devMajor = 0;
    header.devMinor = 0;

    return header;
  }
}
