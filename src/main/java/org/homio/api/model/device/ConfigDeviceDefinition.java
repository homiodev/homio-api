package org.homio.api.model.device;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.homio.api.widget.template.WidgetDefinition;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class ConfigDeviceDefinition {

    // for description inside json file only
    private @Nullable String name;
    private @Nullable String icon;
    private @Nullable String iconColor;
    private @Nullable Set<String> models;
    private @Nullable Set<String> endpoints;
    private @Nullable List<WidgetDefinition> widgets;
    private @Nullable JsonNode options;

    @Getter
    @Setter
    public static class ModelGroups {

        private String name;
        private Set<String> models;
    }
}
