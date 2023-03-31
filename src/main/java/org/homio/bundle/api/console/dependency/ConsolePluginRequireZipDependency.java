package org.homio.bundle.api.console.dependency;

import org.homio.bundle.api.console.ConsolePlugin;
import org.homio.bundle.api.entity.dependency.DependencyZipInstaller;

public interface ConsolePluginRequireZipDependency<T> extends ConsolePlugin<T>, DependencyZipInstaller {

}
