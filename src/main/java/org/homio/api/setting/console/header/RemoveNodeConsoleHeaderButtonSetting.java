package org.homio.api.setting.console.header;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.EntityContext;
import org.homio.api.setting.SettingPlugin;
import org.homio.api.ui.UI;
import org.homio.api.ui.field.UIFieldType;
import org.homio.api.util.CommonUtils;
import org.homio.api.setting.console.header.RemoveNodeConsoleHeaderButtonSetting.NodeRemoveRequest;
import org.json.JSONObject;

/**
 * 'Remove button' console header button for tree/table console blocks.
 */
public class RemoveNodeConsoleHeaderButtonSetting implements
    ConsoleHeaderSettingPlugin<RemoveNodeConsoleHeaderButtonSetting.NodeRemoveRequest>,
    SettingPlugin<NodeRemoveRequest> {

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
        CommonUtils.putOpt(parameters, "confirm", getConfirmMsg());
        CommonUtils.putOpt(parameters, "title", null);
        return parameters;
    }

    @Override
    @SneakyThrows
    public NodeRemoveRequest parseValue(EntityContext entityContext, String value) {
        return StringUtils.isEmpty(value) ? null : CommonUtils.OBJECT_MAPPER.readValue(value, NodeRemoveRequest.class);
    }

    @Override
    @SneakyThrows
    public String writeValue(NodeRemoveRequest value) {
        return value == null ? "" : CommonUtils.OBJECT_MAPPER.writeValueAsString(value);
    }

    @Getter
    @Setter
    public static class NodeRemoveRequest {
        private String tabID;
        private String nodeID;
    }
}
