package org.touchhome.bundle.api.entity.storage;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.common.model.FileSystemItem;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Log4j2
public abstract class VendorFileSystem<D, FS extends CachedFileSystem<FS, ?, D>, E extends BaseEntity & BaseFileSystemEntity> {
    protected final EntityContext entityContext;
    @Setter
    @Getter
    private D drive;
    @Getter
    private E entity;
    @Getter
    @Setter
    private FS root;
    private long connectionHashCode;

    public VendorFileSystem(E entity, EntityContext entityContext) {
        log.warn("Create FS: <{}> for entity: <{}>", getClass().getSimpleName(), entity.getTitle());
        this.entity = entity;
        this.entityContext = entityContext;
    }

    public void setEntity(E entity) {
        this.entity = entity;
        this.onEntityUpdated();
    }

    protected abstract void onEntityUpdated();

    @SneakyThrows
    public D getDrive() {
        if (drive == null) {
            drive = buildDrive();
        }
        return drive;
    }

    protected D buildDrive() {
        throw new RuntimeException("Must be implemented in subclass");
    }

    public abstract long getTotalSpace();

    public abstract long getUsedSpace();

    public List<ArchiveFormat> getSupportArchiveFormat() {
        return Collections.emptyList();
    }

    public abstract FileSystemItem getArchiveEntries(@NotNull String[] archivePath, @Nullable String password) throws IOException;

    public abstract FileSystemItem upload(@NotNull String[] parentPath, @NotNull String fileName, @NotNull byte[] content,
                                          boolean append, boolean replace) throws Exception;

    public abstract FileSystemItem upload(@NotNull String[] parentPath, @NotNull MultipartFile[] files, boolean replace)
            throws Exception;

    public abstract void delete(@NotNull List<String[]> sourceFilePathList) throws Exception;

    public abstract FileSystemItem copy(@NotNull List<String[]> sourceFilePathList, @NotNull String[] targetFilePath,
                                        boolean removeSource, boolean replaceExisting) throws Exception;

    public abstract FileSystemItem archive(@NotNull List<String[]> sourceFilePathList, @NotNull String[] targetFilePath,
                                           @NotNull String format, @Nullable String level, @Nullable String password,
                                           boolean removeSource) throws Exception;

    public abstract FileSystemItem unArchive(@NotNull String[] sourceFilePath, @NotNull String[] targetFilePath,
                                             @Nullable String password, boolean removeSource, @NotNull String fileHandler)
            throws Exception;

    public abstract FileSystemItem createFolder(@NotNull String[] path, @NotNull String name) throws Exception;

    public abstract FileSystemItem rename(@NotNull String[] path, @NotNull String newName) throws Exception;

    public final boolean restart(boolean force) {
        try {
            if (!force && connectionHashCode == getEntity().getConnectionHashCode()) {
                return true;
            }
            dispose();
            reloadFS();
            getEntity().setStatusOnline();
            connectionHashCode = getEntity().getConnectionHashCode();
            return true;
        } catch (Exception ex) {
            getEntity().setStatusError(ex);
            return false;
        }
    }

    protected void reloadFS() {
        getRoot().updateCache(getDrive());
    }

    public abstract void dispose();

    public DownloadData download(@NotNull String[] path, boolean tryUpdateCache, @Nullable String password) throws Exception {
        if (tryUpdateCache) {
            updateCache(false);
        }
        FS fileToDownload = root.findFileByIdOrName(path[path.length - 1], true);
        return fileToDownload == null ? downloadNotFoundedFile(path[path.length - 1]) : fileToDownload.download(getDrive());
    }

    protected DownloadData downloadNotFoundedFile(@NotNull String fileId) {
        throw new RuntimeException("Not implemented for downloading: " + fileId);
    }

    public void updateCache(boolean force) {
        if (force || root.isOutdated(getDrive())) {
            root.updateCache(getDrive());
        }
    }

    public abstract Collection<FileSystemItem> getChild(@NotNull String[] path);

    public Collection<FileSystemItem> getAllFiles(boolean tryUpdateCache) {
        if (tryUpdateCache) {
            this.updateCache(false);
        }
        return root.getAllFiles();
    }

    public Collection<FileSystemItem> getAllFolders(boolean tryUpdateCache) {
        if (tryUpdateCache) {
            this.updateCache(false);
        }
        return root.getAllFolders();
    }

    @Getter
    @RequiredArgsConstructor
    public static class DownloadData implements Closeable {
        private final String name;
        private final String contentType;
        private final byte[] content;
        private final InputStream inputStream;

        @SneakyThrows
        public byte[] getContent() {
            return inputStream == null ? content : IOUtils.toByteArray(inputStream);
        }

        public InputStream getInputStream() {
            return inputStream == null ? new ByteArrayInputStream(content) : inputStream;
        }

        @Override
        public void close() throws IOException {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ArchiveFormat {
        private final String id;
        private final String name;
        private final List<String> extensions;
    }
}
