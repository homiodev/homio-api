package org.touchhome.bundle.api.ui.field.action;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONObject;
import org.touchhome.bundle.api.ui.field.action.impl.DynamicContextMenuAction;
import org.touchhome.bundle.api.ui.field.action.impl.StatefulContextMenuAction;
import org.touchhome.bundle.api.util.TouchHomeUtils;

@Getter
@Accessors(chain = true)
public class UIActionResponse {
    private final String name;
    @Setter
    private String icon;
    @Setter
    private String iconColor;
    // inputs, ref, value, type, etc...
    private JSONObject metadata = new JSONObject();

    public UIActionResponse(String name) {
        this.name = name;
    }

    public UIActionResponse(DynamicContextMenuAction action) {
        this.name = action.getName();
        this.icon = action.getIcon();
        this.iconColor = action.getIconColor();
        if (!action.getParameters().isEmpty()) {
            this.metadata.put("inputs", action.getParameters());
        }
        if (action instanceof StatefulContextMenuAction) {
            action.getMetadata().put("uiActionType", ((StatefulContextMenuAction) action).getType());
            action.getMetadata().put("value", ((StatefulContextMenuAction) action).getValue());
        }
    }

    public UIActionResponse putOpt(String key, Object value) {
        TouchHomeUtils.putOpt(metadata, key, value);
        return this;
    }
}
