package org.touchhome.bundle.api.fs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.Lang;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.BaseEntityIdentifier;
import org.touchhome.bundle.api.entity.HasJsonData;
import org.touchhome.bundle.api.entity.HasStatusAndMsg;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldRenderAsHTML;
import org.touchhome.bundle.api.ui.field.action.HasDynamicContextMenuActions;
import org.touchhome.bundle.api.ui.field.action.UIContextMenuAction;
import org.touchhome.bundle.api.ui.field.color.UIFieldColorStatusMatch;

import java.util.Map;

public interface BaseFileSystemEntity<T extends BaseFileSystemEntity, FS extends VendorFileSystem>
        extends BaseEntityIdentifier<T>, HasDynamicContextMenuActions, HasStatusAndMsg<T>, HasJsonData<T> {

    boolean requireConfigure();

    FS getFileSystem(EntityContext entityContext);

    @JsonIgnore
    Map<String, FS> getFileSystemMap();

    long getConnectionHashCode();

    @UIField(order = 1, required = true, readOnly = true, hideOnEmpty = true, fullWidth = true, bg = "#334842")
    @UIFieldRenderAsHTML
    default String getDescription() {
        String prefix = getEntityPrefix();
        return requireConfigure() ? Lang.getServerMessage(prefix.substring(0, prefix.length() - 1) + ".description") : null;
    }

    @UIField(order = 70, readOnly = true)
    @UIFieldColorStatusMatch
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    default Status getStatus() {
        return getJsonDataEnum("st", Status.UNKNOWN);
    }

    default T setStatus(Status value) {
        return setJsonDataEnum("st", value);
    }

    @UIField(order = 80, readOnly = true, hideOnEmpty = true)
    default String getStatusMessage() {
        return getJsonData("sm");
    }

    default T setStatusMessage(String value) {
        return setJsonData("sm", value);
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
        this.getFileSystem(entityContext).dispose();
        getFileSystemMap().remove(getEntityID());
    }

    @Override
    default void afterUpdate(EntityContext entityContext) {
        this.getFileSystem(entityContext).setEntity((BaseEntity) this);
    }
}
