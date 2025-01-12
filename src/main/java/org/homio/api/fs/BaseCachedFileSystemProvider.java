package org.homio.api.fs;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.homio.api.Context;
import org.homio.api.entity.storage.BaseFileSystemEntity;
import org.homio.api.fs.BaseCachedFileSystemProvider.BaseFSService;
import org.homio.api.fs.BaseCachedFileSystemProvider.FsFileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class BaseCachedFileSystemProvider<Entity extends BaseFileSystemEntity, FSFile extends FsFileEntity<FSFile>,
  Service extends BaseFSService<FSFile>> implements
  FileSystemProvider {

  protected @NotNull
  final Service service;
  protected @NotNull
  final Context context;
  protected @NotNull
  final LoadingCache<String, List<FSFile>> fileCache;
  protected @NotNull
  final ReentrantLock lock = new ReentrantLock();
  protected @NotNull
  final Condition condition;
  @Getter
  protected @NotNull Entity entity;
  protected long connectionHashCode;

  public BaseCachedFileSystemProvider(@NotNull Entity entity, @NotNull Context context) {
    this.entity = entity;
    this.context = context;
    this.condition = lock.newCondition();

    this.fileCache = CacheBuilder.newBuilder().
      expireAfterWrite(1, TimeUnit.HOURS).build(new CacheLoader<>() {
        public @NotNull List<FSFile> load(@NotNull String id) {
          try {
            return service.readChildren(id);
          } catch (Exception ex) {
            service.recreate();
            return service.readChildren(id);
          }
        }
      });
    this.service = createService();
  }

  public static String fixPath(Path path) {
    return fixPath(path.toString());
  }

  public static String fixPath(String path) {
    return SystemUtils.IS_OS_WINDOWS ? path.replace("\\", "/") : path;
  }

  public static String appendSlash(String path) {
    return path.startsWith("/") ? path : "/" + path;
  }

  @Override
  public long getTotalSpace() {
    return 0;
  }

  @Override
  public long getUsedSpace() {
    return 0;
  }

  @Override
  @SneakyThrows
  public boolean exists(@NotNull String id) {
    return getFSFile(id) != null;
  }

  @Override
  @SneakyThrows
  public long size(@NotNull String id) {
    FSFile fsFile = getFSFile(id);
    if (fsFile != null) {
      Long size = fsFile.getSize();
      return size == null ? 0 : size;
    }
    return 0;
  }

  @Override
  public boolean restart(boolean force) {
    try {
      if (!force && connectionHashCode == entity.getConnectionHashCode()) {
        return true;
      }
      dispose();
      getChildren(entity.getFileSystemRoot());
      entity.setStatusOnline();
      connectionHashCode = entity.getConnectionHashCode();
      return true;
    } catch (Exception ex) {
      entity.setStatusError(ex);
      return false;
    }
  }

  @Override
  public void setEntity(@NotNull Object entity) {
    this.entity = (Entity) entity;
    restart(false);
  }

  @Override
  @SneakyThrows
  public @NotNull InputStream getEntryInputStream(@NotNull String id) {
    try (InputStream stream = service.getInputStream(id)) {
      return new ByteArrayInputStream(IOUtils.toByteArray(stream));
    }
  }

  @SneakyThrows
  @Override
  public @NotNull Set<TreeNode> toTreeNodes(@NotNull Set<String> ids) {
    Set<TreeNode> fmPaths = new HashSet<>();
    for (String id : ids) {
      FSFile fsFile = getFSFile(id);
      if (fsFile != null) {
        fmPaths.add(buildTreeNode(fsFile, true));
      }
    }
    return fmPaths;
  }

  @SneakyThrows
  @Override
  public @NotNull TreeNode delete(@NotNull Set<String> ids) {
    List<FSFile> files = new ArrayList<>();
    for (String id : ids) {
      if (id.isEmpty()) {
        throw new IllegalStateException("Path must be specified");
      }
      FSFile fsFile = getFSFile(id);
      if (fsFile != null) {
        if (service.rm(fsFile)) {
          this.fileCache.invalidate(getFileParentId(fsFile));
          files.add(fsFile);
        }
      }
    }
    return buildRoot(files, false);
  }

  @Override
  @SneakyThrows
  public @Nullable TreeNode create(@NotNull String parentId, @NotNull String name, boolean isDir, UploadOption uploadOption) {
    String fullPath = fixPath(Paths.get(parentId).resolve(name));

    if (uploadOption != UploadOption.Replace) {
      FSFile existedFile = getFSFile(fullPath);
      if (existedFile != null) {
        if (uploadOption == UploadOption.SkipExist) {
          return null;
        } else if (uploadOption == UploadOption.Error) {
          throw new FileAlreadyExistsException("File " + name + " already exists");
        }
      }
    }
    if (isDir) {
      service.mkdir(fullPath);
    } else {
      service.put(new ByteArrayInputStream(new byte[0]), fullPath);
    }
    this.fileCache.invalidate(StringUtils.defaultIfEmpty(parentId, getNullParentId()));
    return buildRoot(Collections.singleton(getFSFile(fullPath)), true);
  }

  @SneakyThrows
  @Override
  public @Nullable TreeNode rename(@NotNull String id, @NotNull String newName, UploadOption uploadOption) {
    List<FSFile> files = fileCache.get(id);
    if (files.size() == 1) {
      FSFile file = files.get(0);

      file.rename(newName);

      if (uploadOption != UploadOption.Replace) {
        FSFile existedFile = getFSFile(newName);
        if (existedFile != null) {
          if (uploadOption == UploadOption.SkipExist) {
            return null;
          } else if (uploadOption == UploadOption.Error) {
            throw new FileAlreadyExistsException("File " + newName + " already exists");
          }
        }
      }

      service.rename(file.getFilename(), newName);
      fileCache.invalidate(getFileParentId(file));

      return buildRoot(Collections.singleton(file), false);
    }
    throw new IllegalStateException("File '" + id + "' not found");
  }

  @Override
  public @NotNull TreeNode copy(@NotNull Collection<TreeNode> entries,
                                @NotNull String targetId,
                                @NotNull UploadOption uploadOption) {
    List<FSFile> result = new ArrayList<>();
    copyEntries(entries, targetId, uploadOption, result);
    fileCache.invalidateAll();
    return buildRoot(result, true);
  }

  @Override
  public @Nullable Set<TreeNode> loadTreeUpToChild(@Nullable String parent, @NotNull String id) {
    FSFile file = getFSFile(id);
    if (file == null) {
      return null;
    }
    Set<TreeNode> rootChildren = getChildren(entity.getFileSystemRoot());
    Set<TreeNode> currentChildren = rootChildren;
    List<String> items = StreamSupport.stream(Paths.get(file.getAbsolutePathWithoutRoot()).spliterator(), false)
      .map(Path::toString).toList();
    for (int i = 0; i < items.size() - 1; i++) {
      String pathItem = items.get(i);
      TreeNode foundedObject = currentChildren
        .stream()
        .filter(c -> c.getName().equals(pathItem)).findAny()
        .orElseThrow(() -> new IllegalStateException("Unable find object: " + pathItem));
      currentChildren = getChildren(pathItem);
      foundedObject.addChildren(currentChildren);
    }

    return rootChildren;
  }

  @Override
  @SneakyThrows
  public @NotNull Set<TreeNode> getChildren(@NotNull String parentId) {
    List<FSFile> files = fileCache.get(appendRoot(parentId));
    Stream<FSFile> stream = files.stream();
    if (!entity.isShowHiddenFiles()) {
      stream = stream.filter(s -> !s.getFilename().startsWith("."));
    }
    return stream.map(file -> buildTreeNode(file, true)).collect(Collectors.toSet());
  }

  @Override
  @Nullable
  public Set<TreeNode> getChildrenRecursively(@NotNull String parentId) {
    TreeNode root = new TreeNode();
    buildTreeNodeRecursively(parentId, root);
    return root.getChildren();
  }

  @Override
  public void clearCache() {
    fileCache.invalidateAll();
  }

  public String appendRoot(String id) {
    if (!id.startsWith(entity.getFileSystemRoot())) {
      return fixPath(Paths.get(entity.getFileSystemRoot()).resolve(id));
    }
    return id;
  }

  protected abstract @NotNull Service createService();

  protected @NotNull String getNullParentId() {
    return "/";
  }

  protected void buildTreeNodeExternal(TreeNode treeNode, FSFile file) {
  }

  @NotNull
  private String getFileParentId(FSFile fsFile) {
    FSFile parent = fsFile.getParent(true);
    return parent == null ? getNullParentId() : parent.getId();
  }

  @SneakyThrows
  private void buildTreeNodeRecursively(@NotNull String parentId, @NotNull TreeNode parent) {
    for (FSFile file : fileCache.get(appendRoot(parentId))) {
      TreeNode child = parent.addChild(buildTreeNode(file, true));
      buildTreeNodeRecursively(file.getAbsolutePathWithoutRoot(), child);
    }
  }

  @SneakyThrows
  private @NotNull TreeNode buildRoot(@NotNull Collection<FSFile> result, boolean handleAttributes) {
    TreeNode rootPath = new TreeNode(true, false, "", "", 0L, 0L, null, null);
    for (FSFile ftpFile : result) {
      TreeNode cursor = rootPath;

      //build parent directories
      FSFile parent = ftpFile;
      List<FSFile> stack = new ArrayList<>();
      while ((parent = parent.getParent(true)) != null) {
        stack.add(parent);
      }
      for (FSFile fsFile : Lists.reverse(stack)) {
        cursor = cursor.addChild(buildTreeNode(fsFile, handleAttributes));
      }
      cursor.addChild(buildTreeNode(ftpFile, handleAttributes));
    }
    return rootPath;
  }

  @SneakyThrows
  private TreeNode buildTreeNode(@NotNull FSFile file, boolean handleAttributes) {
    boolean isDirectory = !handleAttributes || file.isDirectory();
    TreeNode treeNode = new TreeNode(
      isDirectory,
      isDirectory && !file.hasChildren(),
      file.getFilename(),
      file.getId(),
      handleAttributes ? file.getSize() : null,
      handleAttributes ? file.getModifiedDateTime() : null,
      this,
      handleAttributes ? file.getContentType() : null);
    buildTreeNodeExternal(treeNode, file);
    return treeNode;
  }

  @SneakyThrows
  private void copyEntries(@NotNull Collection<TreeNode> entries,
                           @NotNull String targetId,
                           @NotNull UploadOption uploadOption,
                           @NotNull List<FSFile> result) {
    for (TreeNode entry : entries) {
      String path = fixPath(Paths.get(targetId).resolve(entry.getName()));
      if (entry.getAttributes().isDir()) {
        service.mkdir(path);
        result.add(service.getFile(path));
        copyEntries(entry.getFileSystem().getChildren(entry), path, uploadOption, result);
      } else {
        try (InputStream stream = entry.getInputStream()) {
          if (uploadOption == UploadOption.Append) {
            byte[] prependContent = IOUtils.toByteArray(service.getInputStream(path));
            byte[] content = Bytes.concat(prependContent, IOUtils.toByteArray(stream));
            service.put(new ByteArrayInputStream(content), path);
          } else {
            service.put(stream, path);
          }
          result.add(service.getFile(path));
        }
      }
    }
  }

  private @Nullable FSFile getFSFile(String id) {
    FSFile fsFile = null;
    try {
      String fileId = appendSlash(appendRoot(id));
      String parentId = StringUtils.defaultIfEmpty(fixPath(Paths.get(fileId).getParent()), getNullParentId());
      fsFile = fileCache.get(parentId).stream()
        .filter(c -> {
          if (c.getId().equals(fileId)) {
            return true;
          }
          String path = appendSlash(fixPath(appendRoot(c.getId())));
          return path.equals(fileId);
        })
        .findAny().orElse(null);
      if (fsFile == null) {
        fsFile = service.getFile(fileId);
        if (fsFile != null) {
          fileCache.invalidate(parentId);
        }
      }
    } catch (Exception ignored) {
    }
    return fsFile;
  }

  public interface BaseFSService<FSFile> {

    void close();

    @NotNull
    InputStream getInputStream(@NotNull String id) throws Exception;

    void mkdir(@NotNull String id) throws Exception;

    void put(@NotNull InputStream inputStream, @NotNull String id) throws Exception;

    void rename(@NotNull String oldName, @NotNull String newName) throws Exception;

    @Nullable
    FSFile getFile(@NotNull String id) throws Exception;

    List<FSFile> readChildren(@NotNull String parentId);

    boolean rm(@NotNull FSFile fsFile);

    /**
     * Recreate service in case of exception
     */
    void recreate();
  }

  public interface FsFileEntity<FSFile> {

    @NotNull
    default String getId() {
      return appendSlash(fixPath(getAbsolutePath()));
    }

    @NotNull
    default String getFilename() {
      return Paths.get(getAbsolutePath()).getFileName().toString();
    }

    @NotNull
    String getAbsolutePath();

    /**
     * Method get absolute path without root and append '/' at begin
     *
     * @return absolute path without root
     */
    @NotNull
    default String getAbsolutePathWithoutRoot() {
      String root = appendSlash(getEntity().getFileSystemRoot());
      String path = appendSlash(fixPath(getAbsolutePath()));
      if (path.startsWith(root)) {
        path = path.substring(root.length());
      }
      return appendSlash(path);
    }

    boolean isDirectory() throws Exception;

    @Nullable
    Long getSize() throws Exception;

    @Nullable
    Long getModifiedDateTime() throws Exception;

    /**
     * Get parent file
     *
     * @param stub - if return full file with attributes, etc.. or just a stub
     * @return - file or null
     */
    @Nullable
    FSFile getParent(boolean stub);

    default boolean isPathEmpty(Path path) {
      if (path == null) {
        return true;
      }
      String pathStr = path.toString();
      if (pathStr.isEmpty()) {
        return true;
      }
      if (pathStr.length() == 1) {
        if (pathStr.equals("\\") || pathStr.equals("/") || pathStr.equals(".")) {
          return true;
        }
      }
      return pathStr.equals(Paths.get(getEntity().getFileSystemRoot()).toString());
    }

    boolean hasChildren();

    BaseFileSystemEntity getEntity();

    default String getContentType() {
      return null;
    }

    default void rename(String newName) {

    }
  }
}
