package org.homio.api.model.endpoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.homio.api.ContextVar.VariableType;
import org.homio.api.model.Icon;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.homio.api.ui.field.action.v1.UIInputEntity;
import org.homio.api.ui.field.inline.UIFieldInlineEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@NoArgsConstructor
public class DeviceEndpointUI implements Comparable<DeviceEndpointUI>, UIFieldInlineEntities.InlineEntity {

    @JsonIgnore
    private DeviceEndpoint endpoint;

    public DeviceEndpointUI(@NotNull DeviceEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public static @NotNull List<DeviceEndpointUI> buildEndpoints(@NotNull Collection<? extends DeviceEndpoint> entities) {
        return entities.stream()
                .filter(DeviceEndpoint::isVisible)
                .map(DeviceEndpointUI::new)
                .sorted()
                .collect(Collectors.toList());
    }

    public @Nullable String getVarSource() {
        if (endpoint.isStateless()) {
            return null;
        }
        try {
            return endpoint.getVariableID() == null ? null :
                    endpoint.context().var().buildDataSource(endpoint.getVariableID());
        } catch (Exception ignore) { // in case if we deleted entity and variables but still send updates to ui
            return null;
        }
    }

    @Override
    public @NotNull String getEntityID() {
        return endpoint.getEntityID();
    }

    public @NotNull Icon getIcon() {
        return endpoint.getIcon();
    }

    public @Nullable VariableType getVarType() {
        return endpoint.getVariableType();
    }

    public @NotNull String getTitle() {
        return endpoint.getName(false);
    }

    public @Nullable String getDescription() {
        return endpoint.getDescription();
    }

    public @Nullable Collection<UIInputEntity> getActions() {
        UIInputBuilder builder = endpoint.createActionBuilder();
        return builder == null ? null : builder.buildAll();
    }

    public @Nullable Collection<UIInputEntity> getSettings() {
        UIInputBuilder builder = endpoint.createSettingsBuilder();
        return builder == null ? null : builder.buildAll();
    }

    @Override
    public int compareTo(@NotNull DeviceEndpointUI o) {
        return endpoint.compareTo(o.endpoint);
    }
}
