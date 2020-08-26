package org.touchhome.bundle.api.console;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface ConsolePlugin extends Comparable<ConsolePlugin> {

    default List<String> drawPlainString() {
        return null;
    }

    default List<? extends HasEntityIdentifier> drawEntity() {
        return null;
    }

    default Map<String, Class<? extends BundleSettingPlugin>> getHeaderActions() {
        return null;
    }

    default int order() {
        return 0;
    }

    @Override
    default int compareTo(@NotNull ConsolePlugin consolePlugin) {
        return Integer.compare(order(), consolePlugin.order());
    }

    default boolean isEnabled() {
        return true;
    }

    default boolean hasRefreshIntervalSetting() {
        return true;
    }

    default boolean hasFitContentSetting() {
        return true;
    }
}
