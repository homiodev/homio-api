package org.touchhome.bundle.api.ui.field.action.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.touchhome.bundle.api.ui.field.UIFieldType;

import java.util.Map;
import java.util.function.Consumer;

@Getter
@Setter
@Log4j2
@Accessors(chain = true)
public class StatefulContextMenuAction extends DynamicContextMenuAction {
    private final UIFieldType type;
    private final String group;
    private final String subGroup;
    private final boolean collapseGroup;
    private final String collapseGroupIcon;
    private Object value;
    private final Map<String, Consumer<StatefulContextMenuAction>> updateHandlers;

    public StatefulContextMenuAction(String name, String group, String subGroup, boolean collapseGroup,
                                     String collapseGroupIcon, int order, String icon, String iconColor, UIFieldType type,
                                     Consumer<String> action, JSONObject metadata,
                                     Map<String, Consumer<StatefulContextMenuAction>> updateHandlers) {
        super(name, order, jsonObject -> action.accept(jsonObject.optString("value")));
        this.setIcon(icon);
        this.setIconColor(iconColor);
        this.setMetadata(metadata);
        this.group = group;
        this.type = type;
        this.updateHandlers = updateHandlers;
        this.subGroup = subGroup;
        this.collapseGroup = collapseGroup;
        this.collapseGroupIcon = collapseGroupIcon;
    }

    public void addButton(String name, String icon) {
        if (!this.getMetadata().has("buttons")) {
            this.getMetadata().put("buttons", new JSONArray());
        }
        ((JSONArray) this.getMetadata().get("buttons")).put(new JSONObject().put("name", name).put("icon", icon));
    }
}
