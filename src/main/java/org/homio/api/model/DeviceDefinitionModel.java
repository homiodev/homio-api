package org.homio.api.model;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.homio.api.util.CommonUtils.OBJECT_MAPPER;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.homio.api.widget.template.WidgetDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class DeviceDefinitionModel {

    // for description inside json file only
    private @Nullable String name;
    private @Nullable String icon;
    private @Nullable String iconColor;
    private @Nullable Set<String> models;
    private @Nullable Set<String> endpoints;
    private @Nullable List<WidgetDefinition> widgets;
    private @Nullable JsonNode options;

    public static @NotNull String getDeviceIconColor(
        @NotNull List<DeviceDefinitionModel> devices,
        @NotNull String defaultIconColor) {
        return devices.isEmpty() ? defaultIconColor : defaultIfEmpty(devices.get(0).getIconColor(), defaultIconColor);
    }

    public static @NotNull String getDeviceIcon(
        @NotNull List<DeviceDefinitionModel> devices,
        @NotNull String defaultIcon) {
        return devices.isEmpty() ? defaultIcon : defaultIfEmpty(devices.get(0).getIcon(), defaultIcon);
    }

    public static @NotNull JsonNode getDeviceOptions(@NotNull List<DeviceDefinitionModel> devices) {
        JsonNode jsonNode = null;
        if (!devices.isEmpty()) {
            jsonNode = devices.get(0).getOptions();
        }
        return jsonNode == null ? OBJECT_MAPPER.createObjectNode() : jsonNode;
    }

    public static @NotNull List<WidgetDefinition> getDeviceWidgets(@NotNull List<DeviceDefinitionModel> devices) {
        return devices.stream()
                      .filter(d -> d.getWidgets() != null)
                      .flatMap(d -> d.getWidgets().stream()).toList();
    }

    @Getter
    @Setter
    public static class ModelGroups {

        private String name;
        private Set<String> models;
    }
}
