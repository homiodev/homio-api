package org.homio.api.setting;

import java.util.Collection;
import org.homio.api.EntityContext;
import org.homio.api.model.OptionModel;
import org.homio.api.ui.field.UIFieldType;
import org.homio.api.exception.NotFoundException;
import org.json.JSONObject;

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
        return entityContext.getBeansOfType(getType()).stream().filter(p -> p.getClass().getSimpleName().equals(value)).findAny()
                .orElseThrow(() -> new NotFoundException(
                        "Unable to find addon: " + value + " of type: " + getType().getSimpleName()));
    }
}