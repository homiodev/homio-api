package org.homio.api.console;

import org.homio.api.model.ActionResponseModel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface ConsolePluginCommunicator extends ConsolePluginComplexLines {

    @Override
    default @NotNull RenderType getRenderType() {
        return RenderType.comm;
    }

    ActionResponseModel commandReceived(String value);

    void dataReceived(ComplexString data);

    default boolean hasRefreshIntervalSetting() {
        return false;
    }

    @Override
    default ActionResponseModel executeAction(String entityID, JSONObject metadata, JSONObject params) {
        return commandReceived(entityID);
    }
}
