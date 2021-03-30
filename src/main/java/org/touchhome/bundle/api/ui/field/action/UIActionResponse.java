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
    private Boolean disabled;
    // inputs, ref, value, type, etc...
    private JSONObject metadata = new JSONObject();

    public UIActionResponse(String name) {
        this.name = name;
    }

    public UIActionResponse(DynamicContextMenuAction action) {
        this.name = action.getName();
        this.icon = action.getIcon();
        this.iconColor = action.getIconColor();
        this.disabled = action.isDisabled() ? true : null;
        if (!action.getMetadata().isEmpty()) {
            for (String key : JSONObject.getNames(action.getMetadata())) {
                metadata.put(key, action.getMetadata().get(key));
            }
        }

        if (!action.getParameters().isEmpty()) {
            this.metadata.put("inputs", action.getParameters());
        }
        if (action instanceof StatefulContextMenuAction) {
            this.metadata.put("uiActionType", ((StatefulContextMenuAction) action).getType());
            putOpt("value", ((StatefulContextMenuAction) action).getValue());
            putOpt("group", ((StatefulContextMenuAction) action).getGroup());
            putOpt("subGroup", ((StatefulContextMenuAction) action).getSubGroup());
            putOpt("collapseGroupIcon", ((StatefulContextMenuAction) action).getCollapseGroupIcon());
            if (((StatefulContextMenuAction) action).isCollapseGroup()) {
                this.metadata.put("collapseGroup", true);
            }
        }
    }

    public UIActionResponse putOpt(String key, Object value) {
        TouchHomeUtils.putOpt(metadata, key, value);
        return this;
    }
}
