package org.touchhome.bundle.api.setting.console.header;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.setting.SettingPlugin;

public interface ConsoleHeaderSettingPlugin<T> extends SettingPlugin<T> {

    default String getIcon() {
        return "";
    }

    @Override
    default boolean isVisible(EntityContext entityContext) {
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
