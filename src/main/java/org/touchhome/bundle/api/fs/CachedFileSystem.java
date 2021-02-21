package org.touchhome.bundle.api.fs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.touchhome.bundle.api.model.OptionModel;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
    private Map<String, CachedFileSystem> children = new HashMap<>();

    public boolean isOutdated(D driver) {
        if (getParent() == null && !supportRoot) { // root
            return System.currentTimeMillis() - getSource().getLastModifiedTime() > 60000;
        }
        T serverValue = this.readFileFromServer(driver);
        return serverValue == null || serverValue.getLastModifiedTime() > source.getLastModifiedTime();
    }

    protected abstract T readFileFromServer(D driver);

    public void updateCache(D driver) {
        for (T serverSource : searchForChildren(source, driver)) {
            fillFromServer(serverSource, driver);
        }
        source.setLastModifiedTime(System.currentTimeMillis());
    }

    protected abstract S newInstance(T source, S parent);

    protected abstract Collection<T> searchForChildren(T serverSource, D driver);

    public S findFile(String id) {
        if (this.source.getId().equals(id)) {
            return (S) this;
        }
        for (CachedFileSystem child : children.values()) {
            CachedFileSystem folder = child.findFile(id);
            if (folder != null) {
                return (S) folder;
            }
        }
        return null;
    }

    private void fillFromServer(T serverSource, D driver) {
        CachedFileSystem cachedFileSystem = children.get(serverSource.getName());
        boolean b = cachedFileSystem == null || cachedFileSystem.source.getLastModifiedTime() != serverSource.getLastModifiedTime();
        if (b) {
            cachedFileSystem = this.newInstance(serverSource, (S) this);

            if (content != null) {
                content.clear();
            }
            children.put(serverSource.getName(), cachedFileSystem);
            if (serverSource.isFolder() && serverSource.fillDeeper()) {
                for (T child : this.searchForChildren(serverSource, driver)) {
                    cachedFileSystem.fillFromServer(child, driver);
                }
            }
        }
    }

    public final byte[] download(D drive) throws Exception {
        byte[] array = content == null ? null : content.get();
        if (array == null) {
            array = downloadContent(drive);
            if (array.length < FileUtils.ONE_MB) {
                content = new WeakReference<>(array);
            }
        }
        return array;
    }

    protected abstract byte[] downloadContent(D drive) throws Exception;

    public Collection<OptionModel> getAllFiles() {
        return getFiles(cachedFileSystem -> true);
    }

    public Collection<OptionModel> getAllFolders() {
        return getFiles(cachedFileSystem -> cachedFileSystem.getSource().isFolder());
    }

    public Collection<OptionModel> getFiles(Predicate<S> predicate) {
        OptionModel parent = new OptionModel();
        fetchOptionModel((S) this, parent, predicate);
        return parent.getChildren();
    }

    private void fetchOptionModel(S parentCacheFile, OptionModel parentModel, Predicate<S> predicate) {
        Collection<S> values = parentCacheFile.getChildren().values();
        for (S cachedFileSystem : values) {
            if (predicate.test(cachedFileSystem)) {
                OptionModel item = OptionModel.of(cachedFileSystem.getSource().getId(), cachedFileSystem.getSource().getName());
                parentModel.addChild(item);
                fetchOptionModel(cachedFileSystem, item, predicate);
            }
        }
    }

    public Path getPath() {
        return getParent() == null ? Paths.get("/") : getParent().getPath().resolve(getSource().getId());
    }

    public interface SourceFileCapability {
        String getId();

        String getName();

        long getLastModifiedTime();

        void setLastModifiedTime(long time);

        boolean isFolder();

        default boolean fillDeeper() {
            return true;
        }
    }

    @Override
    public String toString() {
        return source.getName();
    }
}
