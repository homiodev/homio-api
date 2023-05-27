package org.homio.api.setting;

import org.homio.api.EntityContext;
import org.homio.api.model.OptionModel;

public interface SettingPluginOptionsRemovable<T> extends SettingPluginOptions<T> {

    boolean removableOption(OptionModel optionModel);

    void removeOption(EntityContext entityContext, String key) throws Exception;
}
