package org.touchhome.bundle.api.entity.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.touchhome.common.model.FileSystemItem;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

@RequiredArgsConstructor
public abstract class CachedFileSystem<S extends CachedFileSystem, T extends CachedFileSystem.SourceFileCapability, D> {
    @Getter
    private final T source;
    @Getter
    private final S parent;
    private final boolean supportRoot;
    private WeakReference<byte[]> content;
    @Getter
    private Map<String, S> children = new HashMap<>();

    public boolean isOutdated(D driver) {
        if (getParent() == null && !supportRoot) { // root
            return System.currentTimeMillis() - getSource().getLastModifiedTime() > 60000;
        }
        T serverValue = this.readFileFromServer(driver);
        return serverValue == null || serverValue.getLastModifiedTime() > source.getLastModifiedTime();
    }

    protected abstract T readFileFromServer(D driver);

    public void updateCache(D driver) {
        Set<String> itemsToRemove = new HashSet<>(children.keySet());
        for (T serverSource : searchForChildren(source, driver)) {
            fillFromServer(serverSource, driver, 0);
            itemsToRemove.remove(serverSource.getId());
        }
        children.keySet().removeAll(itemsToRemove);
        source.setLastModifiedTime(System.currentTimeMillis());
    }

    protected int getFSMaxLevel() {
        return 10;
    }

    protected abstract S newInstance(T source, S parent);

    protected abstract Collection<T> searchForChildren(T serverSource, D driver);

    public S findFileByPath(String[] path) {
        return findFileById(path[path.length - 1]);
    }

    public S findFileByPath(String path) {
        return findFileByPath(path.split("/"));
    }

    public S findFileByIdOrName(String nameOrId, boolean recursively) {
        if (this.source.getId().equals(nameOrId) || this.source.getName().equals(nameOrId)) {
            return (S) this;
        }
        for (Map.Entry<String, S> entry : children.entrySet()) {
            if (entry.getKey().equals(nameOrId)) {
                return entry.getValue();
            }
            if (recursively) {
                S result = (S) entry.getValue().findFileByIdOrName(nameOrId, recursively);
                if (result != null) {
                    return result;
                }
            } else {
                return children.values().stream()
                        .filter(c -> c.getSource().getId().equals(nameOrId) || c.getSource().getName().equals(nameOrId)).findAny()
                        .orElse(null);
            }
        }
        return null;
    }

    public S findFileById(String id) {
        if (this.source.getId().equals(id)) {
            return (S) this;
        }
        for (S child : children.values()) {
            CachedFileSystem folder = child.findFileById(id);
            if (folder != null) {
                return (S) folder;
            }
        }
        return null;
    }

    private void fillFromServer(T serverSource, D driver, int level) {
        if (level > getFSMaxLevel()) {
            return;
        }
        CachedFileSystem cachedFileSystem = children.get(serverSource.getId());
        if (cachedFileSystem == null || cachedFileSystem.source.getLastModifiedTime() != serverSource.getLastModifiedTime()) {
            cachedFileSystem = this.newInstance(serverSource, (S) this);

            if (content != null) {
                content.clear();
            }
            children.put(serverSource.getId(), (S) cachedFileSystem);
            if (serverSource.isFolder() && serverSource.fillDeeper()) {
                for (T child : this.searchForChildren(serverSource, driver)) {
                    cachedFileSystem.fillFromServer(child, driver, level + 1);
                }
            }
        }
    }

    public VendorFileSystem.DownloadData download(D drive) throws Exception {
        byte[] array = content == null ? null : content.get();
        if (array == null) {
            array = downloadContent(drive);
            if (array.length < FileUtils.ONE_MB) {
                content = new WeakReference<>(array);
            }
        }
        return new VendorFileSystem.DownloadData(this.source.getName(), null, array, null);
    }

    protected abstract byte[] downloadContent(D drive) throws Exception;

    public Collection<FileSystemItem> getAllFiles() {
        return getFiles(cachedFileSystem -> true);
    }

    public Collection<FileSystemItem> getAllFolders() {
        return getFiles(cachedFileSystem -> cachedFileSystem.getSource().isFolder());
    }

    public Collection<FileSystemItem> getFiles(Predicate<S> predicate) {
        FileSystemItem parent = new FileSystemItem();
        fetchOptionModel((S) this, parent, predicate);
        return parent.getChildren();
    }

    private void fetchOptionModel(S parentCacheFile, FileSystemItem parentModel, Predicate<S> predicate) {
        Collection<S> values = parentCacheFile.getChildren().values();
        for (S cachedFileSystem : values) {
            if (predicate.test(cachedFileSystem)) {
                SourceFileCapability src = cachedFileSystem.getSource();
                boolean folder = src.isFolder();
                FileSystemItem item = new FileSystemItem(folder, false, src.getName(), src.getId(), folder ? 0 : src.size(),
                        src.getLastModifiedTime(), null);
                parentModel.addChild(item);
                fetchOptionModel(cachedFileSystem, item, predicate);
            }
        }
    }

    public Path getPath() {
        return getParent() == null ? Paths.get("/") : getParent().getPath().resolve(getSource().getId());
    }

    @Override
    public String toString() {
        return source.getName();
    }

    public interface SourceFileCapability {
        String getId();

        String getName();

        Long getLastModifiedTime();

        boolean setLastModifiedTime(long time);

        boolean isFolder();

        Long size();

        default boolean fillDeeper() {
            return true;
        }
    }
}
