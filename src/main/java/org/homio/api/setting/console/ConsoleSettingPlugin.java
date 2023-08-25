package org.homio.api.setting.console;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.homio.api.console.ConsolePlugin;
import org.homio.api.console.ConsolePlugin.RenderType;
import org.homio.api.setting.SettingPlugin;

/**
 * Interface for Console settings that enable to filter settings depend on current active tab Class that implement this interface must starts with 'Console'
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface ConsoleSettingPlugin<T> extends SettingPlugin<T> {

    /**
     * @return list of console page names where this setting enabled
     */
    default String[] pages() {
        return null;
    }

    default RenderType[] renderTypes() {
        return null;
    }

    default boolean acceptConsolePluginPage(ConsolePlugin consolePlugin) {
        return false;
    }
}
