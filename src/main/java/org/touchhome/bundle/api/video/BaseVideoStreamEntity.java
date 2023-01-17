package org.touchhome.bundle.api.video;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.springframework.data.util.Pair;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.EntityContextSetting;
import org.touchhome.bundle.api.entity.DeviceBaseEntity;
import org.touchhome.bundle.api.exception.ProhibitedExecution;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.ui.UISidebarMenu;
import org.touchhome.bundle.api.ui.action.UIActionHandler;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldIgnore;
import org.touchhome.bundle.api.ui.field.action.HasDynamicContextMenuActions;
import org.touchhome.bundle.api.ui.field.action.v1.UIInputBuilder;
import org.touchhome.bundle.api.ui.field.color.UIFieldColorStatusMatch;

@Setter
@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@UISidebarMenu(
        icon = "fas fa-video",
        order = 1,
        parent = UISidebarMenu.TopSidebarMenu.MEDIA,
        bg = "#5950A7",
        allowCreateNewItems = true,
        overridePath = "vstreams")
public abstract class BaseVideoStreamEntity<T extends BaseVideoStreamEntity>
        extends DeviceBaseEntity<T> implements HasDynamicContextMenuActions {

    @UIField(order = 300, hideInView = true)
    public boolean isHasAudioStream() {
        return getJsonData("hasAudioStream", false);
    }

    public T setHasAudioStream(boolean value) {
        setJsonData("hasAudioStream", value);
        return (T) this;
    }

    @UIField(order = 11, hideInEdit = true)
    @UIFieldColorStatusMatch
    public Status getSourceStatus() {
        return EntityContextSetting.getStatus(this, "cam_stat", Status.UNKNOWN);
    }

    public void setSourceStatus(Status status, String message) {
        EntityContextSetting.setStatus(this, "cam_stat", "SourceStatus", status, message);
    }

    @UIField(order = 20, hideInEdit = true, hideOnEmpty = true)
    public String getSourceStatusMessage() {
        return EntityContextSetting.getMessage(this, "cam_stat");
    }

    @Override
    @JsonIgnore
    @UIFieldIgnore
    public String getPlace() {
        throw new ProhibitedExecution();
    }

    public abstract byte[] getLastSnapshot();

    protected abstract void fireUpdateSnapshot(EntityContext entityContext, JSONObject params);

    public abstract UIInputBuilder assembleActions();

    public abstract Collection<Pair<String, String>> getVideoSources();

    public abstract String getStreamUrl(String key);

    @Override
    public void assembleActions(UIInputBuilder uiInputBuilder) {
        uiInputBuilder.from(assembleActions());
        uiInputBuilder.fireFetchValues();
    }

    public static class UpdateSnapshotActionHandler implements UIActionHandler {

        @Override
        public ActionResponseModel handleAction(EntityContext entityContext, JSONObject params) {
            BaseVideoStreamEntity entity = entityContext.getEntity(params.getString("entityID"));
            entity.fireUpdateSnapshot(entityContext, params);
            return null;
        }
    }
}
