package org.homio.api.setting;

import java.util.Collection;
import org.homio.api.Context;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface SettingPluginOptions<T> extends SettingPlugin<T> {

    @NotNull Collection<OptionModel> getOptions(Context context, JSONObject params);

    default boolean lazyLoad() {
        return false;
    }

    @Override
    default @NotNull SettingType getSettingType() {
        return viewAsButton() ? SettingType.SelectBoxButton : SettingType.SelectBox;
    }

    default boolean viewAsButton() {
        return false;
    }
}
