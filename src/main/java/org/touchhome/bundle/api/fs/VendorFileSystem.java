package org.touchhome.bundle.api.fs;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.util.Collection;

@Log4j2
public abstract class VendorFileSystem<D, FS extends CachedFileSystem<FS, ?, D>, E extends BaseEntity & BaseFileSystemEntity> {
    @Setter
    @Getter
    private D drive;

    @Getter
    private E entity;

    @Getter
    @Setter
    private FS root;

    private long connectionHashCode;

    protected final EntityContext entityContext;

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

    public abstract void upload(String[] parentPath, String fileName, byte[] content, boolean append) throws Exception;

    public abstract boolean delete(String[] path) throws Exception;

    public final boolean restart(boolean force) {
        try {
            if (!force && connectionHashCode == getEntity().getConnectionHashCode()) {
                return true;
            }
            dispose();
            reloadFS();
            entityContext.updateStatus(getEntity(), Status.ONLINE, null);
            connectionHashCode = getEntity().getConnectionHashCode();
            return true;
        } catch (Exception ex) {
            entityContext.updateStatus(getEntity(), Status.ERROR, TouchHomeUtils.getErrorMessage(ex));
            return false;
        }
    }

    protected void reloadFS() {
        getRoot().updateCache(getDrive());
    }

    public abstract void dispose();

    public byte[] download(String[] path, boolean tryUpdateCache) throws Exception {
        if (tryUpdateCache) {
            updateCache(false);
        }
        FS fileToDownload = root.findFileByIdOrName(path[path.length - 1], true);
        return fileToDownload == null ? downloadNotFoundedFile(path[path.length - 1]) : fileToDownload.download(getDrive());
    }

    protected byte[] downloadNotFoundedFile(String fileId) {
        throw new RuntimeException("Not implemented for downloading: " + fileId);
    }

    public void updateCache(boolean force) {
        if (force || root.isOutdated(getDrive())) {
            root.updateCache(getDrive());
        }
    }

    public Collection<OptionModel> getAllFiles(boolean tryUpdateCache) {
        if (tryUpdateCache) {
            this.updateCache(false);
        }
        return root.getAllFiles();
    }

    public Collection<OptionModel> getAllFolders(boolean tryUpdateCache) {
        if (tryUpdateCache) {
            this.updateCache(false);
        }
        return root.getAllFolders();
    }
}
