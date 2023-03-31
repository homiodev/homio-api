package org.homio.bundle.api.setting;

import java.util.Collection;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.model.KeyValueEnum;
import org.homio.bundle.api.model.OptionModel;
import org.homio.bundle.api.ui.field.UIFieldType;
import org.json.JSONObject;

public interface SettingPluginOptionsEnum<T extends Enum<T>> extends SettingPluginOptions<T> {
    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.SelectBox;
    }

    @Override
    default Collection<OptionModel> getOptions(EntityContext entityContext, JSONObject params) {
        if (KeyValueEnum.class.isAssignableFrom(getType())) {
            return OptionModel.list((Class<? extends KeyValueEnum>) getType());
        }
        return allowEmpty() ? OptionModel.enumWithEmpty(getType()) : OptionModel.enumList(getType());
    }

    default boolean allowEmpty() {
        return false;
    }
}
