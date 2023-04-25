package org.homio.bundle.api.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.homio.bundle.api.entity.storage.BaseFileSystemEntity;
import org.homio.bundle.api.fs.TreeNode;
import org.homio.bundle.api.fs.archive.ArchiveUtil;
import org.homio.bundle.api.model.OptionModel;

@Getter
@Accessors(chain = true)
public class TreeConfiguration {
    private final String id;
    private final String name;
    private String icon;
    private String color;
    private boolean hasDelete;
    private boolean hasRename;
    private boolean hasUpload;
    private boolean hasCreateFile;
    private boolean hasCreateFolder;
    private List<String> editableExtensions;
    private List<OptionModel> zipExtensions;

    @Setter
    private Set<TreeNode> children;

    @Setter
    private String dynamicUpdateId; // unique id for dynamic update tree on UI

    public TreeConfiguration(String id, String name, Set<TreeNode> children) {
        this.id = id;
        this.name = name;
        this.children = children;
    }

    public TreeConfiguration(BaseFileSystemEntity fs) {
        this.id = fs.getEntityID();
        DeviceBaseEntity entity = (DeviceBaseEntity) fs;
        this.name = StringUtils.left(entity.getTitle(), 20);
        this.icon = fs.getFileSystemIcon();
        this.color = fs.getFileSystemIconColor();
        this.hasDelete = true;
        this.hasRename = true;
        this.hasUpload = true;
        this.hasCreateFile = true;
        this.hasCreateFolder = true;

        this.zipExtensions =
            Stream.of(ArchiveUtil.ArchiveFormat.values()).map(f -> OptionModel.of(f.getName()))
                  .collect(Collectors.toList());
        this.editableExtensions =
            Arrays.asList("txt", "java", "cpp", "sh", "css", "scss", "js", "json", "xml", "html", "php", "py", "ts",
                "ino", "conf", "service", "md", "png", "jpg", "jpeg");
    }

    public void setIcon(String icon, String color) {
        this.icon = icon;
        this.color = color;
    }
}