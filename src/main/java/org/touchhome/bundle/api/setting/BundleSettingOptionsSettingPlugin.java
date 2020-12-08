package org.touchhome.bundle.api.setting;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.KeyValueEnum;
import org.touchhome.bundle.api.model.OptionModel;

import java.util.Collection;

public interface BundleSettingOptionsSettingPlugin<T> extends BundleSettingPlugin<T> {

    default Collection<OptionModel> loadAvailableValues(EntityContext entityContext) {
        if (KeyValueEnum.class.isAssignableFrom(getType())) {
            return OptionModel.list((Class<? extends KeyValueEnum>) getType());
        } else if (getType().isEnum()) {
            return OptionModel.enumList((Class<? extends Enum>) getType());
        }
        throw new IllegalStateException("Must be implemented in sub-classes");
    }
}
