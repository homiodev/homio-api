package org.homio.api.setting.console.header.dynamic;

import org.homio.api.setting.console.header.ConsoleHeaderSettingPlugin;

public interface DynamicConsoleHeaderSettingPlugin<T> extends ConsoleHeaderSettingPlugin<T> {

    String getKey();

    String getTitle();
}
