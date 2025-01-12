package org.homio.api.fs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.homio.api.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TreeNode implements Comparable<TreeNode> {

  private @NotNull String name;
  private @Nullable String id;
  private @NotNull TreeNodeAttributes attributes = new TreeNodeAttributes();

  @JsonIgnore
  private @Nullable TreeNode parent;

  @JsonIgnore
  private @Nullable Map<String, TreeNode> childrenMap;

  @JsonIgnore
  private @Nullable InputStream inputStream;

  @JsonIgnore
  private @Nullable FileSystemProvider fileSystem;

  public TreeNode(boolean dir, boolean empty, @NotNull String name, @Nullable String id,
                  @Nullable Long size, @Nullable Long lastModifiedTime,
                  @Nullable FileSystemProvider fileSystem, @Nullable String contentType) {
    this.id = id;
    this.name = name;
    this.fileSystem = fileSystem;

    this.attributes.dir = dir;
    this.attributes.empty = empty;
    this.attributes.lastUpdated = lastModifiedTime;
    this.attributes.size = size;
    this.attributes.contentType = contentType;
  }

  // TreeNode not related to saved data yet
  @SneakyThrows
  public static TreeNode of(@NotNull MultipartFile file) {
    String name = StringUtils.defaultIfEmpty(file.getOriginalFilename(), file.getName());
    return new TreeNode(false, false, name, name, file.getSize(), null, null, file.getContentType()).setInputStream(
      file.getInputStream());
  }

  public static TreeNode of(@NotNull String name, byte[] content) {
    return new TreeNode(false, false, name, name, (long) content.length, null, null, null).setInputStream(
      new ByteArrayInputStream(content));
  }

  @SneakyThrows
  public static TreeNode of(@Nullable String id, @NotNull Path path, @NotNull FileSystemProvider fileSystem) {
    return new TreeNode(false, false, path.getFileName().toString(), id, Files.size(path),
      Files.getLastModifiedTime(path).toMillis(), fileSystem, null);
  }

  /**
   * Build TreeNode tree.
   *
   * @param treePath - example: aaa/bbb. 2 level tree
   * @return Pair(root, lastChild)
   */
  public static @NotNull Pair<TreeNode, TreeNode> buildTree(@NotNull Path treePath) {
    TreeNode overRoot = new TreeNode();
    TreeNode cursor = overRoot;
    for (Path path : treePath) {
      String name = path.getFileName().toString();
      cursor = cursor.addChild(new TreeNode(true, false, name, name, null, null, null, null));
    }
    return Pair.of(overRoot.getChildren().iterator().next(), cursor);
  }

  public void refreshOnUI(Context context) {
    if (fileSystem != null) {
      context.ui().sendDynamicUpdate("tree-%s-%d".formatted(fileSystem.getFileSystemId(), fileSystem.getFileSystemAlias()), this);
    }
  }

  public void merge(TreeNode update) {
    this.attributes = update.attributes;
    if (this.childrenMap == null) {
      this.childrenMap = update.childrenMap;
    } else if (update.childrenMap != null) {
      for (Entry<String, TreeNode> entry : update.childrenMap.entrySet()) {
        TreeNode thisTreeNode = this.childrenMap.get(entry.getKey());
        if (thisTreeNode != null) {
          thisTreeNode.merge(entry.getValue());
        } else {
          this.childrenMap.put(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  @Override
  public String toString() {
    return id;
  }

  public void visitTree(@NotNull Consumer<TreeNode> supplier) {
    supplier.accept(this);
    if (childrenMap != null) {
      for (TreeNode child : childrenMap.values()) {
        child.visitTree(supplier);
      }
    }
  }

  // Convert TreeNode tree to plan Path list
  public @NotNull List<Path> toPath(@NotNull Path basePath) {
    List<Path> paths = new ArrayList<>();
    this.toPath(basePath, paths);
    return paths;
  }

  public @Nullable Set<TreeNode> getChildren() {
    return childrenMap == null ? null : new HashSet<>(childrenMap.values());
  }

  public @Nullable Set<TreeNode> getChildren(boolean loadFromFileSystem) {
    if (loadFromFileSystem && childrenMap == null && fileSystem != null) {
      for (TreeNode child : fileSystem.getChildren(this.getId())) {
        addChild(child);
      }
    }
    return childrenMap == null ? null : new HashSet<>(childrenMap.values());
  }

  public @Nullable TreeNode clone(boolean includeRelations) {
    return new TreeNode(this.name, this.id, attributes, includeRelations ? this.parent : null,
      includeRelations ? childrenMap : null, inputStream, fileSystem);
  }

  public @NotNull TreeNode addChild(@NotNull String id, boolean appendParentId, @NotNull Supplier<TreeNode> supplier) {
    if (this.childrenMap == null) {
      childrenMap = new HashMap<>();
    }
    if (appendParentId) {
      id = this.id == null ? id : this.id + "/" + id;
    }
    if (!childrenMap.containsKey(id)) {
      TreeNode child = supplier.get();
      child.id = id;
      child.fileSystem = this.fileSystem;
      child.parent = this;
      childrenMap.put(id, child);
      // modifyChildrenKeys(this.id, child);
    }
    attributes.setDir(true);
    attributes.setEmpty(false);
    return childrenMap.get(id);
  }

  public @Nullable TreeNode getChild(@NotNull String id) {
    return childrenMap == null ? null : childrenMap.get(id);
  }

  public @Nullable TreeNode getChild(@NotNull Path path) {
    TreeNode cursor = this;
    for (Path child : path) {
      Set<TreeNode> children = cursor.getChildren(true);
      cursor = children == null ? null :
        children.stream().filter(c -> c.name.equals(child.getFileName().toString())).findAny().orElse(null);
      if (cursor == null) {
        return null;
      }
    }
    return cursor;
  }

  public @Nullable TreeNode addChild(@Nullable TreeNode child) {
    return addChild(child, false);
  }

  public @Nullable TreeNode addChild(@Nullable TreeNode child, boolean appendParentId) {
    if (child != null) {
      return addChild(requireNonNull(child.id), appendParentId, () -> child);
    }
    return null;
  }

  public @Nullable TreeNode addChildIfNotFound(@NotNull String id, @NotNull Supplier<TreeNode> childSupplier,
                                               boolean appendParentId) {
    TreeNode treeNode = appendParentId && this.id != null ? this.getChild(this.id + "/" + id) : this.getChild(id);
    if (treeNode == null) {
      return this.addChild(childSupplier.get(), appendParentId);
    }
    return treeNode;
  }

  public @NotNull TreeNode addChildren(@NotNull Collection<TreeNode> children) {
    for (TreeNode child : children) {
      addChild(child);
    }
    return this;
  }

  public void remove() {
    if (parent != null && parent.childrenMap != null) {
      parent.childrenMap.remove(getId());
    }
  }

  @Override
  public int compareTo(@NotNull TreeNode other) {
    boolean thisIsDir = this.attributes.isDir();
    // Compare based on type (folder or file)
    if (thisIsDir != other.getAttributes().isDir()) {
      return thisIsDir ? -1 : 1; // Folders come before files
    }
    // Both are either folders or files, compare alphabetically by name
    return this.name.compareTo(other.name);
  }

  @JsonIgnore
  public @NotNull InputStream getInputStream() {
    if (inputStream == null) {
      inputStream = requireNonNull(fileSystem).getEntryInputStream(requireNonNull(this.id));
    }
    return inputStream;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TreeNode that = (TreeNode) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return requireNonNull(id).hashCode();
  }

  private void toPath(@NotNull Path basePath, @NotNull List<Path> paths) {
    Path newBasePath = basePath.resolve(requireNonNull(id));
    paths.add(basePath.resolve(id));
    if (childrenMap != null) {
      for (TreeNode value : childrenMap.values()) {
        value.toPath(newBasePath, paths);
      }
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TreeNodeAttributes {

    private boolean dir;
    private @Nullable Long size;
    private @Nullable String type;
    private @Nullable Long lastUpdated;
    private boolean empty;
    private @Nullable String contentType;
    private @Nullable JSONObject meta;

    // for UI
    private @Nullable String icon;
    private @Nullable String color;
    private @Nullable Boolean readOnly;
    private @Nullable List<Text> texts; // text array next to name!
    private @Nullable List<TreeNodeChip> chips;
    private boolean removed; // for server -> client updates marking that node has to be removed

    public void addChip(@NotNull TreeNodeChip treeNodeChip) {
      if (chips == null) {
        chips = new ArrayList<>();
      }
      chips.add(treeNodeChip);
    }

    public void setChips(@NotNull TreeNodeChip... chips) {
      if (this.chips == null) {
        this.chips = new ArrayList<>();
      }
      this.chips.clear();
      Collections.addAll(this.chips, chips);
    }

    public @NotNull TextBuilder textsBuilder() {
      texts = new ArrayList<>();
      return new TextBuilder(texts);
    }
  }

  @RequiredArgsConstructor
  public static class TextBuilder {

    private final @NotNull List<Text> texts;

    public void addText(@NotNull String text, @Nullable String color) {
      this.texts.add(new Text(text, color));
    }
  }

  public record Text(@NotNull String text, @Nullable String color) {

  }
}
