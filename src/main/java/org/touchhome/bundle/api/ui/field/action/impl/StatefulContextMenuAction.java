package org.touchhome.bundle.api.ui.field.action.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.touchhome.bundle.api.ui.field.UIFieldType;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
@Setter
@Log4j2
@Accessors(chain = true)
public class StatefulContextMenuAction extends DynamicContextMenuAction {
    private final UIFieldType type;
    private String value;
    private final Supplier<String> getter;

    public StatefulContextMenuAction(String name, int order, String icon, String iconColor, UIFieldType type,
                                     Consumer<String> action, JSONObject metadata, Supplier<String> getter) {
        super(name, order, jsonObject -> action.accept(jsonObject.getString("value")));
        this.setIcon(icon);
        this.setIconColor(iconColor);
        this.setMetadata(metadata);
        this.type = type;
        this.getter = getter;
    }

    public void addButton(String name, String icon) {
        if (!this.getMetadata().has("buttons")) {
            this.getMetadata().put("buttons", new JSONArray());
        }
        ((JSONArray) this.getMetadata().get("buttons")).put(new JSONObject().put("name", name).put("icon", icon));
    }
}
