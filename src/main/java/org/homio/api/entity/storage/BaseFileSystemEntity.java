package org.homio.api.entity.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.NotImplementedException;
import org.homio.api.Context;
import org.homio.api.entity.BaseEntityIdentifier;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.fs.FileSystemProvider;
import org.homio.api.fs.TreeConfiguration;
import org.homio.api.fs.TreeNode;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.Icon;
import org.homio.api.ui.field.*;
import org.homio.api.ui.field.action.HasDynamicContextMenuActions;
import org.homio.api.ui.field.action.UIContextMenuAction;
import org.homio.api.ui.field.selection.SelectionConfiguration;
import org.homio.api.util.CommonUtils;
import org.homio.api.util.Lang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;

public interface BaseFileSystemEntity<FS extends FileSystemProvider>
        extends BaseEntityIdentifier, HasDynamicContextMenuActions, HasStatusAndMsg,
        HasPathAlias {

    Map<String, Map<Integer, FileSystemProvider>> fileSystemMap = new HashMap<>();

    static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    default boolean createView(@NotNull List<TreeNode> sources, @NotNull String name, @NotNull Icon icon) {
        throw new NotImplementedException("View not implemented for fileSystem");
    }

    @JsonIgnore
    default @NotNull List<TreeConfiguration> buildFileSystemConfiguration() {
        List<TreeConfiguration> configurations = new ArrayList<>(3);
        configurations.add(new TreeConfiguration(this));
        for (Alias alias : getAliases()) {
            configurations.add(new TreeConfiguration(this, alias.getName(), alias.getAlias(), alias.getIcon()));
        }
        for (TreeConfiguration configuration : configurations) {
            configuration.setDynamicUpdateId("tree-%s-%d".formatted(configuration.getId(), configuration.getAlias()));
        }

        return configurations;
    }

    @NotNull
    String getFileSystemRoot();

    // in minutes
    default int getFileSystemCacheTimeout() {
        return 1;
    }

    /**
     * @return Short FS alias
     */
    @JsonIgnore
    @NotNull
    String getFileSystemAlias();

    /**
     * @return Does show fs in file manager console tab
     */
    @JsonIgnore
    boolean isShowInFileManager();

    @UIField(order = 1)
    @UIFieldIconPicker(allowEmptyIcon = true)
    @UIFieldGroup(value = "FS", order = 20, borderColor = "#329DBA")
    default String getFileSystemIcon() {
        return getJsonData("fsi", getDefaultFileSystemIcon());
    }

    default void setFileSystemIcon(String value) {
        setJsonData("fsi", value);
    }

    default String getDefaultFileSystemIcon() {
        if (this instanceof SelectionConfiguration sc) {
            return sc.getSelectionIcon().getIcon();
        }
        var route = CommonUtils.getClassRoute(getClass());
        if (route != null) {
            return route.icon();
        }
        return "fas fa-computer";
    }

    @UIField(order = 2)
    @UIFieldColorPicker
    @UIFieldGroup("FS")
    default String getFileSystemIconColor() {
        return getJsonData("fsic", getDefaultFileSystemIconColor());
    }

    default void setFileSystemIconColor(String value) {
        setJsonData("fsic", value);
    }

    default String getDefaultFileSystemIconColor() {
        if (this instanceof SelectionConfiguration sc) {
            return sc.getSelectionIcon().getColor();
        }
        var route = CommonUtils.getClassRoute(getClass());
        if (route != null) {
            return route.color();
        }
        return "#B32317";
    }

    @UIField(order = 40, hideInEdit = true, hideOnEmpty = true)
    @UIFieldProgress
    default UIFieldProgress.Progress getSpace() {
        try {
            FileSystemSize dbSize = requestDbSize();
            if (dbSize != null && dbSize.totalSpace > 0) {
                int usedPercentage = (int) ((dbSize.totalSpace - dbSize.freeSpace) * 100 / dbSize.totalSpace);
                String msg = humanReadableByteCountSI(dbSize.freeSpace) + " free of " + humanReadableByteCountSI(dbSize.totalSpace);
                return new UIFieldProgress.Progress(usedPercentage, 100, msg, true);
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    @JsonIgnore
    boolean requireConfigure();

    default @NotNull FS getFileSystem(@NotNull Context context, int alias) {
        String key = getEntityID();
        var fsMap = fileSystemMap.computeIfAbsent(key, s -> new HashMap<>());
        if (!fsMap.containsKey(alias)) {
            fsMap.put(alias, buildFileSystem(context, alias));
        }
        return (FS) fsMap.get(alias);
    }

    @NotNull
    FS buildFileSystem(@NotNull Context context, int alias);

    @JsonIgnore
    long getConnectionHashCode();

    @UIField(order = 1, hideInEdit = true, hideOnEmpty = true, fullWidth = true, bg = "#334842", type = UIFieldType.HTML)
    default String getDescription() {
        String prefix = getEntityPrefix();
        return requireConfigure() ? Lang.getServerMessage(prefix.substring(0, prefix.length() - 1) + ".description") : null;
    }

    @UIContextMenuAction(value = "RESTART_FS", icon = "fas fa-file-invoice", iconColor = "#418121")
    default ActionResponseModel restart(Context context) {
        var fsMap = fileSystemMap.computeIfAbsent(getEntityID(), s -> new HashMap<>());
        fsMap.values().forEach(d -> d.restart(true));
        return ActionResponseModel.showSuccess("Success restarted");
    }

    @Override
    default void afterDelete() {
        var fsMap = fileSystemMap.remove(getEntityID());
        if (fsMap != null) {
            fsMap.values().forEach(FileSystemProvider::dispose);
        }
    }

    @Override
    default void afterUpdate() {
        var fsMap = fileSystemMap.computeIfAbsent(getEntityID(), s -> new HashMap<>());
        fsMap.values().forEach(d -> d.setEntity(this));
    }

    boolean isShowHiddenFiles();

    /**
     * List of archive formats that able to expand
     *
     * @return - list of archive extensions
     */
    default @NotNull Set<String> getSupportArchiveFormats() {
        return Collections.emptySet();
    }

    default @Nullable FileSystemSize requestDbSize() {
        try {
            FS fileSystem = getFileSystem(context(), 0);
            Long totalSpace = fileSystem.getTotalSpace();
            Long usedSpace = fileSystem.getUsedSpace();
            if (totalSpace != null && usedSpace != null) {
                return new FileSystemSize(totalSpace, totalSpace - usedSpace);
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    record FileSystemSize(long totalSpace, long freeSpace) {
    }

}
