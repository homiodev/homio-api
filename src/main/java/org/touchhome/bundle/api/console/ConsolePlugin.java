package org.touchhome.bundle.api.console;

import org.json.JSONObject;
import org.touchhome.bundle.api.BundleEntryPoint;
import org.touchhome.bundle.api.setting.console.header.ConsoleHeaderSettingPlugin;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Uses for implementing page for console tab
 */
public interface ConsolePlugin<T> extends Comparable<ConsolePlugin<?>> {

    default String getName() {
        return getEntityID();
    }

    default String getEntityID() {
        return BundleEntryPoint.getBundleName(getClass());
    }

    T getValue();

    default JSONObject getOptions() {
        return null;
    }

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
    default Map<String, Class<? extends ConsoleHeaderSettingPlugin<?>>> getHeaderActions() {
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
        lines, comm, table, string, editor
    }
}
