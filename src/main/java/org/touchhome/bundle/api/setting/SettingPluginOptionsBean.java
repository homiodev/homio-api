package org.touchhome.bundle.api.setting;

import java.util.Collection;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.field.UIFieldType;
import org.touchhome.common.exception.NotFoundException;

public interface SettingPluginOptionsBean<T> extends SettingPluginOptions<T> {
    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.SelectBoxDynamic;
    }

    @Override
    default Collection<OptionModel> getOptions(EntityContext entityContext, JSONObject params) {
        return OptionModel.simpleNamelist(entityContext.getBeansOfType(getType()));
    }

    @Override
    default boolean lazyLoad() {
        return true;
    }

    @Override
    default T parseValue(EntityContext entityContext, String value) {
        return entityContext.getBeansOfType(getType()).stream()
                .filter(p -> p.getClass().getSimpleName().equals(value))
                .findAny()
                .orElseThrow(
                        () ->
                                new NotFoundException(
                                        "Unable to find bundle: "
                                                + value
                                                + " of type: "
                                                + getType().getSimpleName()));
    }
}
