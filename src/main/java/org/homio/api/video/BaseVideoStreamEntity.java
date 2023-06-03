package org.homio.api.video;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.homio.api.EntityContext;
import org.homio.api.entity.DeviceBaseEntity;
import org.homio.api.exception.ProhibitedExecution;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.action.UIActionHandler;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldIgnore;
import org.homio.api.ui.field.action.HasDynamicContextMenuActions;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.json.JSONObject;

@SuppressWarnings({"unused", "unchecked"})
@Setter
@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@UISidebarMenu(icon = "fas fa-video", order = 1, parent = UISidebarMenu.TopSidebarMenu.MEDIA,
               bg = "#5950A7", allowCreateNewItems = true, overridePath = "vstreams")
public abstract class BaseVideoStreamEntity<T extends BaseVideoStreamEntity> extends DeviceBaseEntity<T>
        implements HasDynamicContextMenuActions {

    @UIField(order = 300, hideInView = true)
    public boolean isHasAudioStream() {
        return getJsonData("hasAudioStream", false);
    }

    public T setHasAudioStream(boolean value) {
        setJsonData("hasAudioStream", value);
        return (T) this;
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

    public abstract Set<String> getVideoSources();

    public abstract String getStreamUrl(String key);

    @Override
    public void assembleActions(UIInputBuilder uiInputBuilder) {
        uiInputBuilder.from(assembleActions());
        uiInputBuilder.fireFetchValues();
    }

    public static class UpdateSnapshotActionHandler implements UIActionHandler {

        @Override
        public ActionResponseModel handleAction(EntityContext entityContext, JSONObject params) {
            BaseVideoStreamEntity entity = entityContext.getEntityRequire(params.getString("entityID"));
            entity.fireUpdateSnapshot(entityContext, params);
            return null;
        }
    }
}
