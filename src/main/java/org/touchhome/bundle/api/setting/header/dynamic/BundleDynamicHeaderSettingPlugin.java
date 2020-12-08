package org.touchhome.bundle.api.setting.header.dynamic;

import org.touchhome.bundle.api.setting.header.BundleHeaderSettingPlugin;

public interface BundleDynamicHeaderSettingPlugin<T> extends BundleHeaderSettingPlugin<T> {

    String getKey();

    String getTitle();
}
