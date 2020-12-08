package org.touchhome.bundle.api.console;

import org.touchhome.bundle.api.model.ActionResponseModel;

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
