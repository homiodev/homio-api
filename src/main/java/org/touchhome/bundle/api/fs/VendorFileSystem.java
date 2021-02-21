package org.touchhome.bundle.api.fs;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.util.Collection;

public abstract class VendorFileSystem<D, FS extends CachedFileSystem<FS, ?, D>, E extends BaseFileSystemEntity> {
    @Setter
    @Getter
    private D drive;

    @Setter
    @Getter
    private E entity;

    @Getter
    @Setter
    private FS root;

    private long connectionHashCode;

    protected final EntityContext entityContext;

    public VendorFileSystem(E entity, EntityContext entityContext) {
        this.entity = entity;
        this.entityContext = entityContext;
    }

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

    public abstract <P> void upload(String[] parentPath, String fileName, byte[] content, P extraParameter) throws Exception;

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
            updateCache();
        }
        return root.findFile(path[path.length - 1]).download(getDrive());
    }

    public void updateCache() {
        if (root.isOutdated(getDrive())) {
            root.updateCache(getDrive());
        }
    }

    public Collection<OptionModel> getAllFiles(boolean tryUpdateCache) {
        if (tryUpdateCache) {
            this.updateCache();
        }
        return root.getAllFiles();
    }

    public Collection<OptionModel> getAllFolders(boolean tryUpdateCache) {
        if (tryUpdateCache) {
            this.updateCache();
        }
        return root.getAllFolders();
    }
}
