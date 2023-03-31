package org.homio.bundle.api.setting;

import java.util.Collection;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.model.OptionModel;
import org.json.JSONObject;

public interface SettingPluginOptions<T> extends SettingPlugin<T> {

    Collection<OptionModel> getOptions(EntityContext entityContext, JSONObject params);

    default boolean lazyLoad() {
        return false;
    }
}
