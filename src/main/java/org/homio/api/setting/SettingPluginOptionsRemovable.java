package org.homio.api.setting;

import org.homio.api.Context;
import org.homio.api.model.OptionModel;

public interface SettingPluginOptionsRemovable<T> extends SettingPluginOptions<T> {

  boolean removableOption(OptionModel optionModel);

  void removeOption(Context context, String key) throws Exception;
}
