package org.homio.api.entity.device;

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
import org.homio.api.ui.UIActionHandler;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.action.HasDynamicContextMenuActions;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.homio.api.ui.field.inline.UIFieldInlineEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public interface DeviceEndpointsBehaviourContract extends DeviceContract, HasDynamicContextMenuActions {

    @JsonIgnore
    @NotNull String getDeviceFullName();

    @UIFieldGroup("GENERAL")
    default Date getUpdateTime() {
        return DeviceEndpoint.getLastUpdated(getDeviceEndpoints().values());
    }

    @Override
    default ActionResponseModel handleAction(EntityContext entityContext, String actionID, JSONObject params) throws Exception {
        for (DeviceEndpointUI endpoint : getEndpoints()) {
            if (actionID.startsWith(endpoint.getEntityID())) {
                UIInputBuilder actionBuilder = endpoint.getEndpoint().createActionBuilder();
                if (actionBuilder != null) {
                    UIActionHandler actionHandler = actionBuilder.findActionHandler(actionID);
                    if (actionHandler != null) {
                        return actionHandler.handleAction(entityContext, params);
                    }
                }
                UIInputBuilder settingsBuilder = endpoint.getEndpoint().createSettingsBuilder();
                if (settingsBuilder != null) {
                    UIActionHandler settingHandler = settingsBuilder.findActionHandler(actionID);
                    if (settingHandler != null) {
                        return settingHandler.handleAction(entityContext, params);
                    }
                }
            }
        }
        return HasDynamicContextMenuActions.super.handleAction(entityContext, actionID, params);
    }

    @JsonIgnore
    @NotNull Map<String, ? extends DeviceEndpoint> getDeviceEndpoints();

    default @Nullable DeviceEndpoint getDeviceEndpoint(@NotNull String endpoint) {
        return getDeviceEndpoints().get(endpoint);
    }

    @Nullable String getDescription();

    /**
     * Last item updated
     *
     * @return string representation of last item updated
     */
    default @Nullable String getUpdated() {
        DeviceEndpoint endpoint = getDeviceEndpoint(ENDPOINT_LAST_SEEN);
        return endpoint == null ? null : endpoint.getLastValue().stringValue();
    }

    @UIField(order = 9999)
    @UIFieldInlineEntities(bg = "#27FF0005", noContentTitle = "W.ERROR.NO_ENDPOINTS")
    default List<DeviceEndpointUI> getEndpoints() {
        return DeviceEndpointUI.buildEndpoints(getDeviceEndpoints().values());
    }

    @Override
    default @NotNull Icon getEntityIcon() {
        ConfigDeviceDefinitionService service = getConfigDeviceDefinitionService();
        List<ConfigDeviceDefinition> matchDevices = findMatchDeviceConfigurations();
        return new Icon(
            service == null ? "fas fa-server" : service.getDeviceIcon(matchDevices, "fas fa-server"),
            service == null ? UI.Color.random() : service.getDeviceIconColor(matchDevices, UI.Color.random())
        );
    }

    @JsonIgnore
    @Nullable ConfigDeviceDefinitionService getConfigDeviceDefinitionService();

    @JsonIgnore
    @NotNull List<ConfigDeviceDefinition> findMatchDeviceConfigurations();
}
