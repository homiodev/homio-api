package org.homio.api.setting;

import java.util.Collection;
import org.homio.api.EntityContext;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface SettingPluginOptions<T> extends SettingPlugin<T> {

    @NotNull Collection<OptionModel> getOptions(EntityContext entityContext, JSONObject params);

    default boolean lazyLoad() {
        return false;
    }

    @Override
    default @NotNull SettingType getSettingType() {
        return SettingType.SelectBox;
    }
}
