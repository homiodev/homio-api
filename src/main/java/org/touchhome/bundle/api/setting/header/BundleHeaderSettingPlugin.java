package org.touchhome.bundle.api.setting.header;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.setting.BundleSettingPlugin;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface BundleHeaderSettingPlugin<T> extends BundleSettingPlugin<T> {

    @Override
    default boolean isVisible(EntityContext entityContext) {
        return false;
    }

    @Override
    default boolean transientState() {
        return true;
    }
}
