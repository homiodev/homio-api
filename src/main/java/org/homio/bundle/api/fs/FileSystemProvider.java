package org.homio.bundle.api.fs;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FileSystemProvider {

    long getTotalSpace();

    long getUsedSpace();

    default void dispose() {

    }

    boolean restart(boolean force);

    void setEntity(Object entity);

    InputStream getEntryInputStream(@NotNull String id);

    // to unarchive we need download full file to avoid problem with memory
    default Path getArchiveAsLocalPath(@NotNull String id) {
        throw new RuntimeException("Not supported by FileSystem");
    }

    Set<TreeNode> toTreeNodes(@NotNull Set<String> ids);

    default TreeNode toTreeNode(@NotNull String id) {
        return toTreeNodes(Collections.singleton(id)).iterator().next();
    }

    TreeNode delete(@NotNull Set<String> ids);

    TreeNode create(@NotNull String parentId, @NotNull String name, boolean isDir, UploadOption uploadOption);

    TreeNode rename(@NotNull String id, @NotNull String newName, UploadOption uploadOption);

    TreeNode copy(@NotNull Collection<TreeNode> entries, @NotNull String targetId, UploadOption uploadOption);

    default TreeNode copy(@NotNull TreeNode entry, @NotNull String targetId, UploadOption uploadOption) {
        return copy(Collections.singletonList(entry), targetId, uploadOption);
    }

    default Set<TreeNode> loadTreeUpToChild(@NotNull String id) {
        return loadTreeUpToChild(null, id);
    }

    Set<TreeNode> loadTreeUpToChild(@Nullable String parent, @NotNull String id);

    // get one level children. If archive - return full list with sub children
    @NotNull Set<TreeNode> getChildren(@NotNull String parentId);

    Set<TreeNode> getChildrenRecursively(@NotNull String parentId);

    default Set<TreeNode> getChildren(@NotNull TreeNode treeNode) {
        if (treeNode.getChildren() != null) {
            return treeNode.getChildren();
        }
        return getChildren(treeNode.getId());
    }

    default void clearCache() {
    }

    enum UploadOption {
        Replace, Append, Error, SkipExist
    }
}
