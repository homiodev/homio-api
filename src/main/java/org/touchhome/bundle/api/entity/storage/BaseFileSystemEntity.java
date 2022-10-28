package org.touchhome.bundle.api.entity.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.*;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldType;
import org.touchhome.bundle.api.ui.field.action.HasDynamicContextMenuActions;
import org.touchhome.bundle.api.ui.field.action.UIContextMenuAction;
import org.touchhome.common.fs.FileSystemProvider;
import org.touchhome.common.util.Lang;

import java.util.HashMap;
import java.util.Map;

public interface BaseFileSystemEntity<T extends BaseEntity & BaseFileSystemEntity, FS extends FileSystemProvider>
        extends BaseEntityIdentifier<T>, HasDynamicContextMenuActions, HasStatusAndMsg<T>, HasJsonData {
    Map<String, FileSystemProvider> fileSystemMap = new HashMap<>();

    default TreeConfiguration buildFileSystemConfiguration(EntityContext entityContext) {
        return new TreeConfiguration(this);
    }

    /**
     * Short FS alias
     */
    @JsonIgnore
    String getFileSystemAlias();

    /**
     * Does show fs in file manager console tab
     */
    @JsonIgnore
    boolean isShowInFileManager();

    @JsonIgnore
    String getFileSystemIcon();

    @JsonIgnore
    String getFileSystemIconColor();

    @JsonIgnore
    boolean requireConfigure();

    default FS getFileSystem(EntityContext entityContext) {
        String key = getEntityID();
        if (!fileSystemMap.containsKey(key)) {
            FS fileSystemProvider = buildFileSystem(entityContext);
            fileSystemMap.put(key, fileSystemProvider);
        }
        return (FS) fileSystemMap.get(key);
    }

    FS buildFileSystem(EntityContext entityContext);

    @JsonIgnore
    long getConnectionHashCode();

    @UIField(order = 1, required = true, readOnly = true, hideOnEmpty = true, fullWidth = true, bg = "#334842",
            type = UIFieldType.HTML)
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
    default void afterDelete(EntityContext entityContext) {
        FileSystemProvider provider = fileSystemMap.remove(getEntityID());
        if (provider != null) {
            provider.dispose();
        }
    }

    @Override
    default void afterUpdate(EntityContext entityContext) {
        this.getFileSystem(entityContext).setEntity(this);
    }
}