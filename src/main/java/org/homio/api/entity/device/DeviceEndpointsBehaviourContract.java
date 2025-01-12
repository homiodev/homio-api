package org.homio.api.entity.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.Context;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.Icon;
import org.homio.api.model.device.ConfigDeviceDefinition;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.model.endpoint.DeviceEndpointUI;
import org.homio.api.ui.UI.Color;
import org.homio.api.ui.UIActionHandler;
import org.homio.api.ui.UISidebarChildren;
import org.homio.api.ui.UISidebarMenu;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.action.HasDynamicContextMenuActions;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.homio.api.ui.field.inline.UIFieldInlineEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.homio.api.model.endpoint.DeviceEndpoint.ENDPOINT_LAST_SEEN;

public interface DeviceEndpointsBehaviourContract extends DeviceContract, HasDynamicContextMenuActions {

  @JsonIgnore
  @NotNull String getDeviceFullName();

  @UIFieldGroup("GENERAL")
  default Date getUpdateTime() {
    return DeviceEndpoint.getLastUpdated(getDeviceEndpoints().values());
  }

  @Override
  default ActionResponseModel handleAction(Context context, String actionID, JSONObject params) throws Exception {
    for (DeviceEndpointUI endpoint : getEndpoints()) {
      if (actionID.startsWith(endpoint.getEntityID())) {
        UIInputBuilder actionBuilder = endpoint.getEndpoint().createActionBuilder();
        if (actionBuilder != null) {
          UIActionHandler actionHandler = actionBuilder.findActionHandler(actionID);
          if (actionHandler != null) {
            return actionHandler.handleAction(context, params);
          }
        }
        UIInputBuilder settingsBuilder = endpoint.getEndpoint().createSettingsBuilder();
        if (settingsBuilder != null) {
          UIActionHandler settingHandler = settingsBuilder.findActionHandler(actionID);
          if (settingHandler != null) {
            return settingHandler.handleAction(context, params);
          }
        }
      }
    }
    return HasDynamicContextMenuActions.super.handleAction(context, actionID, params);
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
    List<ConfigDeviceDefinition> matchDevices = findMatchDeviceConfigurations();
    ConfigDeviceDefinition def = matchDevices.isEmpty() ? null : matchDevices.get(0);
    if (def != null && StringUtils.isNotEmpty(def.getIcon())) {
      return new Icon(def.getIcon(), def.getIconColor());
    }
    String icon = null;
    String color = null;
    UISidebarChildren children = getClass().getAnnotation(UISidebarChildren.class);
    if (children != null) {
      icon = children.icon();
      color = children.color();
    } else {
      UISidebarMenu sidebarMenu = getClass().getAnnotation(UISidebarMenu.class);
      if (sidebarMenu != null) {
        icon = sidebarMenu.icon();
        color = sidebarMenu.bg();
      }
    }
    return new Icon(defaultIfEmpty(icon, "fas fa-server"), defaultIfEmpty(color, Color.random()));
  }

  @JsonIgnore
  @NotNull List<ConfigDeviceDefinition> findMatchDeviceConfigurations();
}
