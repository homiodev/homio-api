package org.homio.bundle.api.console;

import java.util.Map;
import org.homio.bundle.api.BundleEntrypoint;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.model.ActionResponseModel;
import org.homio.bundle.api.setting.console.header.ConsoleHeaderSettingPlugin;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

/**
 * Uses for implementing page for console tab
 */
public interface ConsolePlugin<T> extends Comparable<ConsolePlugin<?>> {

    EntityContext getEntityContext();

    default String getName() {
        return getEntityID();
    }

    default String getEntityID() {
        return BundleEntrypoint.getBundleName(getClass());
    }

    T getValue();

    default JSONObject getOptions() {
        return null;
    }

    RenderType getRenderType();

    /**
     * @return Uses for grouping few bundle pages with same parent
     */
    default String getParentTab() {
        return null;
    }

    /**
     * @return Uses when need header buttons for whole plugin
     */
    default Map<String, Class<? extends ConsoleHeaderSettingPlugin<?>>> getHeaderActions() {
        return null;
    }

    /**
     * @return Draw console titles in such order
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

    default ActionResponseModel executeAction(String entityID, JSONObject metadata, JSONObject params)
            throws Exception {
        return null;
    }

    enum RenderType {
        lines, comm, table, string, editor, tree, frame
    }
}