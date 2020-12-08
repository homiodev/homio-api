package org.touchhome.bundle.api.setting.header;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.setting.BundleSettingPlugin;

public interface BundleHeaderSettingPlugin<T> extends BundleSettingPlugin<T> {

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
