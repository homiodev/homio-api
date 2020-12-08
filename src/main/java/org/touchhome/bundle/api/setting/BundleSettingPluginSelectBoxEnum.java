package org.touchhome.bundle.api.setting;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.KeyValueEnum;
import org.touchhome.bundle.api.json.Option;

import java.util.List;

public interface BundleSettingPluginSelectBoxEnum<T extends Enum<T>> extends BundleSettingPlugin<T> {
    @Override
    default SettingType getSettingType() {
        return SettingType.SelectBox;
    }

    @Override
    default List<Option> loadAvailableValues(EntityContext entityContext) {
        if (KeyValueEnum.class.isAssignableFrom(getType())) {
            return Option.list((Class<? extends KeyValueEnum>) getType());
        }
        return allowEmpty() ? Option.enumWithEmpty(getType()) : Option.enumList(getType());
    }

    default boolean allowEmpty() {
        return false;
    }
}
