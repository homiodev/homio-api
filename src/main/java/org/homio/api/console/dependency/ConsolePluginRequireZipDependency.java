package org.homio.api.console.dependency;

import org.homio.api.console.ConsolePlugin;
import org.homio.api.entity.dependency.DependencyZipInstaller;
import org.json.JSONObject;

public interface ConsolePluginRequireZipDependency<T> extends ConsolePlugin<T>, DependencyZipInstaller {

    @Override
    default void assembleOptions(JSONObject options) {
        options.put("reqDeps", requireInstallDependencies());
    }
}
