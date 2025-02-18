package org.homio.api.fs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public interface FileSystemProvider {

  long getTotalSpace();

  long getUsedSpace();

  int getFileSystemAlias();

  @NotNull String getFileSystemId();

  default void dispose() {

  }

  boolean restart(boolean force);

  void setEntity(@NotNull Object entity);

  boolean exists(@NotNull String id);

  default long size(@NotNull String id) {
    Long size = toTreeNode(id).getAttributes().getSize();
    return size == null ? 0 : size;
  }

  @NotNull
  InputStream getEntryInputStream(@NotNull String id);

  default @NotNull Resource getEntryResource(@NotNull String id) {
    return new InputStreamResource(getEntryInputStream(id));
  }

  // to unarchive we need download full file to avoid problem with memory
  default Path getArchiveAsLocalPath(@NotNull String id) {
    throw new RuntimeException("Not supported by FileSystem");
  }

  @NotNull Set<TreeNode> toTreeNodes(@NotNull Set<String> ids);

  default TreeNode toTreeNode(@NotNull String id) {
    return toTreeNodes(Collections.singleton(id)).iterator().next();
  }

  @NotNull TreeNode delete(@NotNull Set<String> ids);

  @Nullable TreeNode create(@NotNull String parentId, @NotNull String name, boolean isDir, UploadOption uploadOption);

  @Nullable TreeNode rename(@NotNull String id, @NotNull String newName, UploadOption uploadOption);

  @NotNull TreeNode copy(@NotNull Collection<TreeNode> entries, @NotNull String targetId, @NotNull UploadOption uploadOption);

  default @NotNull TreeNode copy(@NotNull TreeNode entry, @NotNull String targetId, @NotNull UploadOption uploadOption) {
    return copy(Collections.singletonList(entry), targetId, uploadOption);
  }

  // method has to search for files/folders in thread
  default @Nullable SearchThread search(@NotNull SearchParameters searchParameters,
                                        @NotNull SearchCallback searchCallback) {
    return () -> {
      // just ignore
    };
  }

  @Nullable
  default Set<TreeNode> loadTreeUpToChild(@NotNull String id) {
    return loadTreeUpToChild(null, id);
  }

  @Nullable Set<TreeNode> loadTreeUpToChild(@Nullable String parent, @NotNull String id);

  // get one level children. If archive - return full list with sub children
  @NotNull
  Set<TreeNode> getChildren(@NotNull String parentId);

  @Nullable Set<TreeNode> getChildrenRecursively(@NotNull String parentId);

  default @NotNull Set<TreeNode> getChildren(@NotNull TreeNode treeNode) {
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

  interface SearchCallback {
    void found(TreeNode treeNode);

    void done();
  }

  interface SearchThread {
    void cancel();
  }

  record SearchParameters(
    // general configs
    int maxResults, // max files return to ui
    int subdirDepth, // folder depth to dive
    boolean searchFolder, // search folders
    boolean searchFiles, // search files
    String searchFor, // file/folder name
    String searchText, // text to search
    boolean caseSensitive, // for text
    boolean revertSearch, // return files that NOT contains text
    boolean wholeWordsOnly, // for text
    boolean searchInArchive) { // for text
  }
}
