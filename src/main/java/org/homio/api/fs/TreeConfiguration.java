package org.homio.api.fs;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.entity.storage.BaseFileSystemEntity;
import org.homio.api.model.Icon;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Accessors(chain = true)
public class TreeConfiguration {

  private final String id;
  private final String name;
  private final int alias;
  @Setter
  private Icon icon;
  private Boolean hasDelete;
  private Boolean hasRename;
  private Boolean hasUpload;
  private Boolean hasCreateFile;
  private Boolean hasCreateFolder;
  private List<String> editableExtensions;
  private Set<String> zipOpenExtensions;
  private List<TreeNodeChip> chips;

  private long freeSize;
  private long totalSize;

  @Setter
  private Set<TreeNode> children;

  @Setter
  private String dynamicUpdateId; // unique id for dynamic update tree on UI

  public TreeConfiguration(String id, String name, Set<TreeNode> children) {
    this.id = id;
    this.alias = -1;
    this.name = name;
    this.children = children;
  }

  public TreeConfiguration(@NotNull BaseFileSystemEntity<?> fs) {
    this.id = fs.getEntityID();
    this.alias = -1;
    this.zipOpenExtensions = fs.getSupportArchiveFormats();
    DeviceBaseEntity entity = (DeviceBaseEntity) fs;
    this.name = StringUtils.left(entity.getTitle(), 20);
    this.icon = fs.getFileSystemIcon();

    makeDefaultFSConfiguration();
  }

  public TreeConfiguration(@NotNull BaseFileSystemEntity<?> fs, @NotNull String path, @NotNull Icon icon) {
    this(fs, Path.of(path).getFileName().toString(), Math.abs(path.hashCode()), icon);
  }

  public TreeConfiguration(@NotNull BaseFileSystemEntity<?> fs, @NotNull String name, int alias, @NotNull Icon icon) {
    this.id = fs.getEntityID();
    this.alias = alias;
    this.zipOpenExtensions = fs.getSupportArchiveFormats();
    this.name = name;
    this.icon = icon;

    makeDefaultFSConfiguration();
  }

  private void makeDefaultFSConfiguration() {
    this.hasDelete = true;
    this.hasRename = true;
    this.hasUpload = true;
    this.hasCreateFile = true;
    this.hasCreateFolder = true;
  }

  public TreeConfiguration setSize(long freeSize, long totalSize) {
    this.freeSize = freeSize;
    this.totalSize = totalSize;
    return this;
  }

  public void addChip(TreeNodeChip treeNodeChip) {
    if (chips == null) {
      chips = new ArrayList<>();
    }
    chips.add(treeNodeChip);
  }
}
