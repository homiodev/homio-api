package org.homio.api.model.endpoint;

import static org.homio.api.ui.field.UIFieldType.HTML;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.homio.api.model.endpoint.DeviceEndpoint.EndpointType;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldTitleRef;
import org.homio.api.ui.field.action.v1.UIInputEntity;
import org.homio.api.ui.field.inline.UIFieldInlineEntityWidth;
import org.jetbrains.annotations.NotNull;

@Getter
public class DeviceEndpointUI implements Comparable<DeviceEndpoint> {

    private final String entityID;
    @UIField(order = 2, type = HTML)
    private final String title;
    private final String valueTitle;
    @JsonIgnore
    private final DeviceEndpoint endpoint;

    public DeviceEndpointUI(DeviceEndpoint endpoint) {
        this.endpoint = endpoint;
        this.entityID = endpoint.getEndpointEntityID();
        String variableID = endpoint.getVariableID();
        if (variableID != null) {
            String varSource = endpoint.getEntityContext().var().buildDataSource(variableID, false);
            this.title =
                ("<div class=\"inline-2row_d\"><div class=\"clickable history-link\" data-hl=\"%s\" style=\"color:%s;\"><i class=\"mr-1 "
                    + "%s\"></i>%s</div><span>%s</div></div>").formatted(
                    varSource, endpoint.getIcon().getColor(), endpoint.getIcon().getIcon(),
                    endpoint.getName(false), endpoint.getDescription());
        } else {
            this.title =
                "<div class=\"inline-2row_d\"><div style=\"color:%s;\"><i class=\"mr-1 %s\"></i>%s</div><span>%s</div></div>".formatted(
                    endpoint.getIcon().getColor(), endpoint.getIcon().getIcon(), endpoint.getName(false), endpoint.getDescription());
        }
        if (endpoint.getEndpointType() == EndpointType.select) {
            this.valueTitle = endpoint.getValue() + "Values: " + String.join(", ", endpoint.getSelectValues());
        } else {
            this.valueTitle = endpoint.getValue().toString();
        }
    }

    public static List<DeviceEndpointUI> build(Collection<DeviceEndpoint> entities) {
        return entities.stream()
                       .filter(DeviceEndpoint::isVisible)
                       .map(DeviceEndpointUI::new)
                       .sorted()
                       .collect(Collectors.toList());
    }

    @UIField(order = 4, style = "margin-left: auto; margin-right: 8px;")
    @UIFieldInlineEntityWidth(30)
    @UIFieldTitleRef("valueTitle")
    public UIInputEntity getValue() {
        return endpoint.createUIInputBuilder().buildAll().iterator().next();
    }


    @Override
    public int compareTo(@NotNull DeviceEndpoint o) {
        return endpoint.compareTo(o);
    }
}
