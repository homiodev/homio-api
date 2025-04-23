package org.homio.api.fs.archive.tar;

import java.util.Date;
import lombok.Getter;

public class TarEntry {

    @Getter
    protected byte[] file;
    @Getter
    protected TarHeader header;
    protected boolean isDirectory;

    private TarEntry() {
        file = null;
        header = new TarHeader();
        isDirectory = false;
    }

    public TarEntry(byte[] file, String entryName, boolean isDirectory) {
        this();
        this.file = file;
        extractTarHeader(entryName);
        this.isDirectory = isDirectory;
    }

    public TarEntry(byte[] headerBuf) {
        this();
        parseTarHeader(headerBuf);
    }

    public TarEntry(TarHeader header) {
        file = null;
        this.header = header;
        file = new byte[0];
    }

    public boolean equals(TarEntry it) {
        return header.name.toString().contentEquals(it.header.name);
    }

    public boolean isDescendent(TarEntry desc) {
        return desc.header.name.toString().startsWith(header.name.toString());
    }

    public String getName() {
        String name = header.name.toString();
        if (header.namePrefix != null
            && !header.namePrefix.toString().equals("")) {
            name = header.namePrefix + "/" + name;
        }

        return name;
    }

    public void setName(String name) {
        header.name = new StringBuffer(name);
    }

    public int getUserId() {
        return header.userId;
    }

    public void setUserId(int userId) {
        header.userId = userId;
    }

    public int getGroupId() {
        return header.groupId;
    }

    public void setGroupId(int groupId) {
        header.groupId = groupId;
    }

    public String getUserName() {
        return header.userName.toString();
    }

    public void setUserName(String userName) {
        header.userName = new StringBuffer(userName);
    }

    public String getGroupName() {
        return header.groupName.toString();
    }

    public void setGroupName(String groupName) {
        header.groupName = new StringBuffer(groupName);
    }

    public void setIds(int userId, int groupId) {
        setUserId(userId);
        setGroupId(groupId);
    }

    public Date getModTime() {
        return new Date(header.modTime * 1000);
    }

    public void setModTime(long time) {
        header.modTime = time / 1000;
    }

    public void setModTime(Date time) {
        header.modTime = time.getTime() / 1000;
    }

    public long getSize() {
        return header.size;
    }

    public void setSize(long size) {
        header.size = size;
    }

    public boolean isDirectory() {
        if (header != null) {
            if (header.linkFlag == TarHeader.LF_DIR) {
                return true;
            }

            return header.name.toString().endsWith("/");
        }

        return false;
    }

    public void extractTarHeader(String entryName) {
        header = TarHeader.createHeader(entryName, file.length,
                header.modTime / 1000, isDirectory);
    }

    public long computeCheckSum(byte[] buf) {
        long sum = 0;

        for (byte element : buf) {
            sum += 255 & element;
        }

        return sum;
    }

    public void writeEntryHeader(byte[] outbuf) {
        int offset = 0;

        offset = TarHeader.getNameBytes(header.name, outbuf, offset,
                TarHeader.NAMELEN);
        offset = Octal.getOctalBytes(header.mode, outbuf, offset,
                TarHeader.MODELEN);
        offset = Octal.getOctalBytes(header.userId, outbuf, offset,
                TarHeader.UIDLEN);
        offset = Octal.getOctalBytes(header.groupId, outbuf, offset,
                TarHeader.GIDLEN);

        long size = header.size;

        offset = Octal.getLongOctalBytes(size, outbuf, offset,
                TarHeader.SIZELEN);
        offset = Octal.getLongOctalBytes(header.modTime, outbuf, offset,
                TarHeader.MODTIMELEN);

        int csOffset = offset;
        for (int c = 0; c < TarHeader.CHKSUMLEN; ++c) {
            outbuf[offset++] = (byte) ' ';
        }

        outbuf[offset++] = header.linkFlag;

        offset = TarHeader.getNameBytes(header.linkName, outbuf, offset,
                TarHeader.NAMELEN);
        offset = TarHeader.getNameBytes(header.magic, outbuf, offset,
                TarHeader.USTAR_MAGICLEN);
        offset = TarHeader.getNameBytes(header.userName, outbuf, offset,
                TarHeader.USTAR_USER_NAMELEN);
        offset = TarHeader.getNameBytes(header.groupName, outbuf, offset,
                TarHeader.USTAR_GROUP_NAMELEN);
        offset = Octal.getOctalBytes(header.devMajor, outbuf, offset,
                TarHeader.USTAR_DEVLEN);
        offset = Octal.getOctalBytes(header.devMinor, outbuf, offset,
                TarHeader.USTAR_DEVLEN);
        offset = TarHeader.getNameBytes(header.namePrefix, outbuf, offset,
                TarHeader.USTAR_FILENAME_PREFIX);

        while (offset < outbuf.length) {
            outbuf[offset++] = 0;
        }

        long checkSum = computeCheckSum(outbuf);

        Octal.getCheckSumOctalBytes(checkSum, outbuf, csOffset,
                TarHeader.CHKSUMLEN);
    }

    public void parseTarHeader(byte[] bh) {
        int offset = 0;

        header.name = TarHeader.parseName(bh, offset, TarHeader.NAMELEN);
        offset += TarHeader.NAMELEN;

        header.mode = (int) Octal.parseOctal(bh, offset, TarHeader.MODELEN);
        offset += TarHeader.MODELEN;

        header.userId = (int) Octal.parseOctal(bh, offset, TarHeader.UIDLEN);
        offset += TarHeader.UIDLEN;

        header.groupId = (int) Octal.parseOctal(bh, offset, TarHeader.GIDLEN);
        offset += TarHeader.GIDLEN;

        header.size = Octal.parseOctal(bh, offset, TarHeader.SIZELEN);
        offset += TarHeader.SIZELEN;

        header.modTime = Octal.parseOctal(bh, offset, TarHeader.MODTIMELEN);
        offset += TarHeader.MODTIMELEN;

        header.checkSum = (int) Octal.parseOctal(bh, offset,
                TarHeader.CHKSUMLEN);
        offset += TarHeader.CHKSUMLEN;

        header.linkFlag = bh[offset++];

        header.linkName = TarHeader.parseName(bh, offset, TarHeader.NAMELEN);
        offset += TarHeader.NAMELEN;

        header.magic = TarHeader
                .parseName(bh, offset, TarHeader.USTAR_MAGICLEN);
        offset += TarHeader.USTAR_MAGICLEN;

        header.userName = TarHeader.parseName(bh, offset,
                TarHeader.USTAR_USER_NAMELEN);
        offset += TarHeader.USTAR_USER_NAMELEN;

        header.groupName = TarHeader.parseName(bh, offset,
                TarHeader.USTAR_GROUP_NAMELEN);
        offset += TarHeader.USTAR_GROUP_NAMELEN;

        header.devMajor = (int) Octal.parseOctal(bh, offset,
                TarHeader.USTAR_DEVLEN);
        offset += TarHeader.USTAR_DEVLEN;

        header.devMinor = (int) Octal.parseOctal(bh, offset,
                TarHeader.USTAR_DEVLEN);
        offset += TarHeader.USTAR_DEVLEN;

        header.namePrefix = TarHeader.parseName(bh, offset,
                TarHeader.USTAR_FILENAME_PREFIX);
    }
}
