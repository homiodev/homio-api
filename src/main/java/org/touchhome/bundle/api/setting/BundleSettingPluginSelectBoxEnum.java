package org.touchhome.bundle.api.setting;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.KeyValueEnum;
import org.touchhome.bundle.api.model.OptionModel;

import java.util.List;

public interface BundleSettingPluginSelectBoxEnum<T extends Enum<T>> extends BundleSettingOptionsSettingPlugin<T> {
    @Override
    default SettingType getSettingType() {
        return SettingType.SelectBox;
    }

    @Override
    default List<OptionModel> loadAvailableValues(EntityContext entityContext) {
        if (KeyValueEnum.class.isAssignableFrom(getType())) {
            return OptionModel.list((Class<? extends KeyValueEnum>) getType());
        }
        return allowEmpty() ? OptionModel.enumWithEmpty(getType()) : OptionModel.enumList(getType());
    }

    default boolean allowEmpty() {
        return false;
    }
}
