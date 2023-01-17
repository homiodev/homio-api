package org.touchhome.bundle.api.setting;

import java.util.Collection;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.KeyValueEnum;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.field.UIFieldType;

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
        return allowEmpty()
                ? OptionModel.enumWithEmpty(getType())
                : OptionModel.enumList(getType());
    }

    default boolean allowEmpty() {
        return false;
    }
}
