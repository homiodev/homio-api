package org.touchhome.bundle.api.console;

import org.touchhome.bundle.api.json.ActionResponse;

public interface ConsolePluginCommunicator extends ConsolePluginComplexLines {

    @Override
    default RenderType getRenderType() {
        return RenderType.comm;
    }

    ActionResponse commandReceived(String value);

    void dataReceived(ComplexString data);

    default boolean hasRefreshIntervalSetting() {
        return false;
    }
}
