package org.homio.api.entity.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.homio.api.EntityContext;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.BaseEntityIdentifier;
import org.homio.api.entity.HasJsonData;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.fs.FileSystemProvider;
import org.homio.api.fs.TreeConfiguration;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.Icon;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldType;
import org.homio.api.ui.field.action.HasDynamicContextMenuActions;
import org.homio.api.ui.field.action.UIContextMenuAction;
import org.homio.api.util.Lang;
import org.jetbrains.annotations.NotNull;

public interface BaseFileSystemEntity<T extends BaseEntity & BaseFileSystemEntity, FS extends FileSystemProvider>
    extends BaseEntityIdentifier<T>, HasDynamicContextMenuActions, HasStatusAndMsg<T>, HasJsonData {

    Map<String, FileSystemProvider> fileSystemMap = new HashMap<>();

    default @NotNull TreeConfiguration buildFileSystemConfiguration(@NotNull EntityContext entityContext) {
        return new TreeConfiguration(this);
    }

    @NotNull String getFileSystemRoot();

    /**
     * @return Short FS alias
     */
    @JsonIgnore
    @NotNull String getFileSystemAlias();

    /**
     * @return Does show fs in file manager console tab
     */
    @JsonIgnore
    boolean isShowInFileManager();

    @JsonIgnore
    @NotNull Icon getFileSystemIcon();

    @JsonIgnore
    boolean requireConfigure();

    default @NotNull FS getFileSystem(@NotNull EntityContext entityContext) {
        String key = getEntityID();
        if (!fileSystemMap.containsKey(key)) {
            FS fileSystemProvider = buildFileSystem(entityContext);
            fileSystemMap.put(key, fileSystemProvider);
        }
        return (FS) fileSystemMap.get(key);
    }

    @NotNull FS buildFileSystem(@NotNull EntityContext entityContext);

    @JsonIgnore
    long getConnectionHashCode();

    @UIField(order = 1, hideInEdit = true, hideOnEmpty = true, fullWidth = true, bg = "#334842", type = UIFieldType.HTML)
    default String getDescription() {
        String prefix = getEntityPrefix();
        return requireConfigure() ? Lang.getServerMessage(prefix.substring(0, prefix.length() - 1) + ".description") : null;
    }

    @UIContextMenuAction(value = "RESTART", icon = "fas fa-power-off")
    default ActionResponseModel restart(EntityContext entityContext) {
        if (this.getFileSystem(entityContext).restart(true)) {
            return ActionResponseModel.showSuccess("Success restarted");
        } else {
            return ActionResponseModel.showSuccess("Restart failed");
        }
    }

    @Override
    default void afterDelete(@NotNull EntityContext entityContext) {
        FileSystemProvider provider = fileSystemMap.remove(getEntityID());
        if (provider != null) {
            provider.dispose();
        }
    }

    @Override
    default void afterUpdate(@NotNull EntityContext entityContext, boolean persist) {
        this.getFileSystem(entityContext).setEntity(this);
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
}
