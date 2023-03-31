package org.homio.bundle.api.setting;

import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.model.OptionModel;

public interface SettingPluginOptionsRemovable<T> extends SettingPluginOptions<T> {

    boolean removableOption(OptionModel optionModel);

    void removeOption(EntityContext entityContext, String key) throws Exception;
}
