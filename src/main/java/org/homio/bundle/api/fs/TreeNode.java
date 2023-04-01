package org.homio.bundle.api.fs;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TreeNode implements Comparable<TreeNode> {

    private String name;
    private String id;
    private TreeNodeAttributes attributes = new TreeNodeAttributes();

    @JsonIgnore
    private TreeNode parent;

    @JsonIgnore
    private Map<String, TreeNode> childrenMap;

    @JsonIgnore
    private InputStream inputStream;

    @JsonIgnore
    private FileSystemProvider fileSystem;

    public TreeNode(boolean dir, boolean empty, String name, String id, Long size, Long lastModifiedTime,
                    FileSystemProvider fileSystem, String contentType) {
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

    public static TreeNode of(@NotNull String name, @NotNull byte[] content) {
        return new TreeNode(false, false, name, name, (long) content.length, null, null, null).setInputStream(
                new ByteArrayInputStream(content));
    }

    @SneakyThrows
    public static TreeNode of(@NotNull String id, @NotNull Path path, @NotNull FileSystemProvider fileSystem) {

        return new TreeNode(false, false, path.getFileName().toString(), id, Files.size(path),
                Files.getLastModifiedTime(path).toMillis(), fileSystem, null);
    }

    /**
     * Build TreeNode tree.
     *
     * @param treePath - example: aaa/bbb. 2 level tree
     * @return Pair(root, lastChild)
     */
    public static Pair<TreeNode, TreeNode> buildTree(@NotNull Path treePath) {
        TreeNode overRoot = new TreeNode();
        TreeNode cursor = overRoot;
        for (Path path : treePath) {
            String name = path.getFileName().toString();
            cursor = cursor.addChild(new TreeNode(true, false, name, name, null, null, null, null));
        }
        return Pair.of(overRoot.getChildren().iterator().next(), cursor);
    }

    public void visitTree(Consumer<TreeNode> supplier) {
        supplier.accept(this);
        if (childrenMap != null) {
            for (TreeNode child : childrenMap.values()) {
                child.visitTree(supplier);
            }
        }
    }

    // Convert TreeNode tree to plan Path list
    public List<Path> toPath(Path basePath) {
        List<Path> paths = new ArrayList<>();
        this.toPath(basePath, paths);
        return paths;
    }

    private void toPath(Path basePath, List<Path> paths) {
        Path newBasePath = basePath.resolve(id);
        paths.add(basePath.resolve(id));
        if (childrenMap != null) {
            for (TreeNode value : childrenMap.values()) {
                value.toPath(newBasePath, paths);
            }
        }
    }

    public Set<TreeNode> getChildren() {
        return childrenMap == null ? null : new HashSet<>(childrenMap.values());
    }

    public Set<TreeNode> getChildren(boolean loadFromFileSystem) {
        if (loadFromFileSystem && childrenMap == null && fileSystem != null) {
            for (TreeNode child : fileSystem.getChildren(this.getId())) {
                addChild(child);
            }
        }
        return childrenMap == null ? null : new HashSet<>(childrenMap.values());
    }

    public TreeNode clone(boolean includeRelations) {
        return new TreeNode(this.name, this.id, attributes, includeRelations ? this.parent : null,
                includeRelations ? childrenMap : null, inputStream, fileSystem);
    }

    public TreeNode addChild(@NotNull String id, boolean appendParentId, @NotNull Supplier<TreeNode> supplier) {
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

    public TreeNode getChild(String id) {
        return childrenMap == null ? null : childrenMap.get(id);
    }

    public TreeNode getChild(Path path) {
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

    public TreeNode addChild(@Nullable TreeNode child) {
        return addChild(child, false);
    }

    public TreeNode addChild(@Nullable TreeNode child, boolean appendParentId) {
        if (child != null) {
            return addChild(child.id, appendParentId, () -> child);
        }
        return null;
    }

    public TreeNode addChildIfNotFound(@NotNull String id, @NotNull Supplier<TreeNode> childSupplier,
                                       boolean appendParentId) {
        TreeNode treeNode = appendParentId && this.id != null ? this.getChild(this.id + "/" + id) : this.getChild(id);
        if (treeNode == null) {
            return this.addChild(childSupplier.get(), appendParentId);
        }
        return treeNode;
    }

    public TreeNode addChildren(@NotNull Collection<TreeNode> children) {
        for (TreeNode child : children) {
            addChild(child);
        }
        return this;
    }

    public void remove() {
        if (parent != null) {
            parent.childrenMap.remove(getId());
        }
    }

    @Override
    public int compareTo(@NotNull TreeNode o) {
        return this.id.compareTo(o.id);
    }

    @JsonIgnore
    public InputStream getInputStream() {
        return inputStream == null ? fileSystem.getEntryInputStream(this.id) : inputStream;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        TreeNode that = (TreeNode) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TreeNodeAttributes {

        private boolean dir;
        private Long size;
        private Long lastUpdated;
        private boolean empty;
        private String contentType;
        private JSONObject meta;

        // for UI
        private String icon;
        private String color;
        private List<Text> texts; // text array next to name!
        private List<Chip> chips;
        private boolean removed; // for server -> client updates marking that node has to be removed

        public void addChip(Chip chip) {
            if (chips == null) {
                chips = new ArrayList<>();
            }
            chips.add(chip);
        }

        public void setChips(Chip... chips) {
            if (this.chips == null) {
                this.chips = new ArrayList<>();
            }
            this.chips.clear();
            Collections.addAll(this.chips, chips);
        }

        public TextBuilder textsBuilder() {
            texts = new ArrayList<>();
            return new TextBuilder(texts);
        }
    }

    @RequiredArgsConstructor
    public static class TextBuilder {

        private final List<Text> texts;

        public void addText(String text, String color) {
            this.texts.add(new Text(text, color));
        }
    }

    /**
     * Represent [Chip] block element on right side of tree. Must specify at least icon or text to be visible on UI.
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    @RequiredArgsConstructor
    public static class Chip {

        @Nullable
        private final String icon;
        @Nullable
        private final String text;
        @Nullable
        private String bgColor;
        @Nullable
        private String iconColor;

        private boolean clickable; // if Chip not only info but communicate with server
        private JSONObject metadata; // require if clickable and need handle Chip on server side
    }

    @Getter
    @RequiredArgsConstructor
    public static class Text {

        private final String text;
        private final String color;
    }
}
