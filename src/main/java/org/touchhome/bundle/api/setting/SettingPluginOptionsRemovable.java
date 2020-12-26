package org.touchhome.bundle.api.setting;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.OptionModel;

public interface SettingPluginOptionsRemovable<T> extends SettingPluginOptions<T> {

    boolean removableOption(OptionModel optionModel);

    void removeOption(EntityContext entityContext, String key) throws Exception;
}
