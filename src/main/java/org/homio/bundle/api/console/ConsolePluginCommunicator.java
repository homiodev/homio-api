package org.homio.bundle.api.console;

import org.homio.bundle.api.model.ActionResponseModel;

public interface ConsolePluginCommunicator extends ConsolePluginComplexLines {

    @Override
    default RenderType getRenderType() {
        return RenderType.comm;
    }

    ActionResponseModel commandReceived(String value);

    void dataReceived(ComplexString data);

    default boolean hasRefreshIntervalSetting() {
        return false;
    }
}
