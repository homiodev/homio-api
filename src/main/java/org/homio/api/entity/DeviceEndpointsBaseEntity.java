package org.homio.api.entity;

import static org.homio.api.model.endpoint.DeviceEndpoint.ENDPOINT_LAST_SEEN;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.homio.api.EntityContext;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.Icon;
import org.homio.api.model.device.ConfigDeviceDefinition;
import org.homio.api.model.device.ConfigDeviceDefinitionService;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.model.endpoint.DeviceEndpointUI;
import org.homio.api.ui.UI;
import org.homio.api.ui.action.UIActionHandler;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.action.HasDynamicContextMenuActions;
import org.homio.api.ui.field.inline.UIFieldInlineEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public abstract class DeviceEndpointsBaseEntity extends DeviceBaseEntity<DeviceEndpointsBaseEntity> implements HasDynamicContextMenuActions {

    @UIFieldGroup("GENERAL")
    public @NotNull Date getUpdateTime() {
        return DeviceEndpoint.getLastUpdated(getDeviceEndpoints().values());
    }

    @Override
    public ActionResponseModel handleAction(EntityContext entityContext, String actionID, JSONObject params) throws Exception {
        for (DeviceEndpointUI endpoint : getEndpoints()) {
            if (actionID.startsWith(endpoint.getEntityID())) {
                UIActionHandler actionHandler = endpoint.getEndpoint().createUIInputBuilder().findActionHandler(actionID);
                if (actionHandler != null) {
                    return actionHandler.handleAction(entityContext, params);
                }
            }
        }
        return HasDynamicContextMenuActions.super.handleAction(entityContext, actionID, params);
    }

    @JsonIgnore
    public abstract @NotNull Map<String, DeviceEndpoint> getDeviceEndpoints();

    public @Nullable DeviceEndpoint getDeviceEndpoint(@NotNull String endpoint) {
        return getDeviceEndpoints().get(endpoint);
    }

    public abstract @Nullable String getDescription();

    @JsonIgnore
    public abstract @NotNull String getModel();

    /**
     * Last item updated
     *
     * @return string representation of last item updated
     */
    public @Nullable String getUpdated() {
        DeviceEndpoint endpoint = getDeviceEndpoint(ENDPOINT_LAST_SEEN);
        return endpoint == null ? null : endpoint.getLastValue().stringValue();
    }

    @UIField(order = 9999)
    @UIFieldInlineEntities(bg = "#27FF0005")
    public List<DeviceEndpointUI> getEndpoints() {
        return DeviceEndpointUI.build(getDeviceEndpoints().values());
    }

    @Override
    public @NotNull Icon getEntityIcon() {
        ConfigDeviceDefinitionService service = getConfigDeviceDefinitionService();
        List<ConfigDeviceDefinition> matchDevices = findMatchDeviceConfigurations();
        return new Icon(
            service.getDeviceIcon(matchDevices, "fas fa-server"),
            service.getDeviceIconColor(matchDevices, UI.Color.random())
        );
    }

    public abstract @NotNull ConfigDeviceDefinitionService getConfigDeviceDefinitionService();

    public abstract @NotNull List<ConfigDeviceDefinition> findMatchDeviceConfigurations();
}
