package org.touchhome.bundle.api.ui.field.action;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONObject;
import org.touchhome.bundle.api.util.TouchHomeUtils;

// TODO:  ???????????????
@Getter
@Accessors(chain = true)
public class UUUUIActionResponse {
    private final String name;
    @Setter
    private String icon;
    @Setter
    private String iconColor;
    private Boolean disabled;
    // inputs, ref, value, type, etc...
    private JSONObject metadata = new JSONObject();

    public UUUUIActionResponse(String name) {
        this.name = name;
    }

    public UUUUIActionResponse putOpt(String key, Object value) {
        TouchHomeUtils.putOpt(metadata, key, value);
        return this;
    }
}
