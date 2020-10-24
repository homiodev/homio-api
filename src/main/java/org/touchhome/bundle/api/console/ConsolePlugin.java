package org.touchhome.bundle.api.console;

import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.setting.BundleSettingPlugin;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface ConsolePlugin extends Comparable<ConsolePlugin> {

    /**
     * Uses for grouping few bundle pages with same parent
     */
    default String getParentTab() {
        return null;
    }

    /**
     * Implement if plugin draw regular string
     */
    default List<String> drawPlainString() {
        return null;
    }

    /**
     * Implement if plugin draw table where item represent row
     */
    default List<? extends HasEntityIdentifier> drawEntity() {
        return null;
    }

    /**
     * Uses when need header buttons for whole plugin
     */
    default Map<String, Class<? extends BundleSettingPlugin>> getHeaderActions() {
        return null;
    }

    /**
     * Draw console titles in such order
     * @return
     */
    default int order() {
        return 0;
    }

    @Override
    default int compareTo(@NotNull ConsolePlugin consolePlugin) {
        return Integer.compare(order(), consolePlugin.order());
    }

    // hide from ui if not enabled
    default boolean isEnabled() {
        return true;
    }

    // enable refresh interval select-box
    default boolean hasRefreshIntervalSetting() {
        return true;
    }
}
