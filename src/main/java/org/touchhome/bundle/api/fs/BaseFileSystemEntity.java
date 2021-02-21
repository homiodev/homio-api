package org.touchhome.bundle.api.fs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.Lang;
import org.touchhome.bundle.api.entity.HasStatusAndMsg;
import org.touchhome.bundle.api.entity.MiscEntity;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldRenderAsHTML;
import org.touchhome.bundle.api.ui.field.action.HasDynamicContextMenuActions;
import org.touchhome.bundle.api.ui.field.action.UIContextMenuAction;
import org.touchhome.bundle.api.ui.field.color.UIFieldColorStatusMatch;

import java.util.Map;

public abstract class BaseFileSystemEntity<T extends BaseFileSystemEntity, FS extends VendorFileSystem>
        extends MiscEntity<T> implements HasDynamicContextMenuActions, HasStatusAndMsg<T> {

    public abstract boolean requireConfigure();

    public abstract FS getFileSystem(EntityContext entityContext);

    @JsonIgnore
    public abstract Map<String, FS> getFileSystemMap();

    public abstract long getConnectionHashCode();

    public abstract String getDefaultName();

    @Override
    public String getName() {
        return StringUtils.defaultString(super.getName(), getDefaultName());
    }

    @UIField(order = 1, required = true, readOnly = true, hideOnEmpty = true, fullWidth = true, bg = "#334842")
    @UIFieldRenderAsHTML
    public String getDescription() {
        return requireConfigure() ? Lang.getServerMessage(getEntityPrefix().substring(0, getEntityPrefix().length() - 1) + ".description") : null;
    }

    @UIField(order = 70, readOnly = true)
    @UIFieldColorStatusMatch
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Status getStatus() {
        return getJsonDataEnum("st", Status.UNKNOWN);
    }

    public T setStatus(Status value) {
        return setJsonDataEnum("st", value);
    }

    @UIField(order = 80, readOnly = true, hideOnEmpty = true)
    public String getStatusMessage() {
        return getJsonData("sm");
    }

    public T setStatusMessage(String value) {
        return setJsonData("sm", value);
    }

    @UIContextMenuAction(value = "RESTART", icon = "fas fa-power-off")
    public ActionResponseModel restart(EntityContext entityContext) {
        if (this.getFileSystem(entityContext).restart(true)) {
            return ActionResponseModel.showSuccess("Success restarted");
        } else {
            return ActionResponseModel.showSuccess("Restart failed");
        }
    }

    @Override
    public void afterDelete(EntityContext entityContext) {
        this.getFileSystem(entityContext).dispose();
        getFileSystemMap().remove(getEntityID());
    }

    @Override
    public void afterUpdate(EntityContext entityContext) {
        this.getFileSystem(entityContext).setEntity(this);
    }
}
