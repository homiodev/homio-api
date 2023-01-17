package org.touchhome.bundle.api.setting;

import java.util.Collection;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.OptionModel;

public interface SettingPluginOptions<T> extends SettingPlugin<T> {

    Collection<OptionModel> getOptions(EntityContext entityContext, JSONObject params);

    default boolean lazyLoad() {
        return false;
    }
}
