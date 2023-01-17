package org.touchhome.bundle.api.setting.console.header;

import static org.touchhome.bundle.api.util.TouchHomeUtils.putOpt;
import static org.touchhome.common.util.CommonUtils.OBJECT_MAPPER;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.setting.SettingPlugin;
import org.touchhome.bundle.api.ui.UI;
import org.touchhome.bundle.api.ui.field.UIFieldType;

/** 'Remove button' console header button for tree/table console blocks. */
public class RemoveNodeConsoleHeaderButtonSetting
        implements ConsoleHeaderSettingPlugin<
                        RemoveNodeConsoleHeaderButtonSetting.NodeRemoveRequest>,
                SettingPlugin<RemoveNodeConsoleHeaderButtonSetting.NodeRemoveRequest> {

    @Override
    public String getIcon() {
        return "fas fa-trash";
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public Class<NodeRemoveRequest> getType() {
        return RemoveNodeConsoleHeaderButtonSetting.NodeRemoveRequest.class;
    }

    @Override
    public String getIconColor() {
        return UI.Color.RED;
    }

    @Override
    public UIFieldType getSettingType() {
        return UIFieldType.Button;
    }

    public String getConfirmMsg() {
        return "REMOVE_NODE_TITLE";
    }

    @Override
    public JSONObject getParameters(EntityContext entityContext, String value) {
        JSONObject parameters = new JSONObject();
        putOpt(parameters, "confirm", getConfirmMsg());
        putOpt(parameters, "title", null);
        return parameters;
    }

    @Override
    @SneakyThrows
    public NodeRemoveRequest parseValue(EntityContext entityContext, String value) {
        return StringUtils.isEmpty(value)
                ? null
                : OBJECT_MAPPER.readValue(value, NodeRemoveRequest.class);
    }

    @Override
    @SneakyThrows
    public String writeValue(NodeRemoveRequest value) {
        return value == null ? "" : OBJECT_MAPPER.writeValueAsString(value);
    }

    @Getter
    @Setter
    public static class NodeRemoveRequest {
        private String tabID;
        private String nodeID;
    }
}
