package org.touchhome.bundle.api.console;

import org.touchhome.bundle.api.setting.BundleSettingPlugin;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Uses for implementing page for console tab
 */
public interface ConsolePlugin<T> extends Comparable<ConsolePlugin<?>> {

    T getValue();

    RenderType getRenderType();

    /**
     * Uses for grouping few bundle pages with same parent
     */
    default String getParentTab() {
        return null;
    }

    /**
     * Uses when need header buttons for whole plugin
     */
    default Map<String, Class<? extends BundleSettingPlugin<?>>> getHeaderActions() {
        return null;
    }

    /**
     * Draw console titles in such order
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

    enum RenderType {
        lines, comm, table, string
    }
}
