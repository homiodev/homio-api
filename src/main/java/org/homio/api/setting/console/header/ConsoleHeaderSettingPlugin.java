package org.homio.api.setting.console.header;

import org.homio.api.Context;
import org.homio.api.setting.SettingPlugin;

public interface ConsoleHeaderSettingPlugin<T> extends SettingPlugin<T> {

    @Override
    default boolean isVisible(Context context) {
        return false;
    }

    @Override
    default boolean transientState() {
        return true;
    }

    default String[] fireActionsBeforeChange() {
        return null;
    }

    @Override
    default int order() {
        return 0;
    }
}
