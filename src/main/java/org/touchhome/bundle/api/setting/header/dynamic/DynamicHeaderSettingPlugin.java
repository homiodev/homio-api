package org.touchhome.bundle.api.setting.header.dynamic;

import org.touchhome.bundle.api.setting.header.HeaderSettingPlugin;

public interface DynamicHeaderSettingPlugin<T> extends HeaderSettingPlugin<T> {

    String getKey();

    String getTitle();
}
