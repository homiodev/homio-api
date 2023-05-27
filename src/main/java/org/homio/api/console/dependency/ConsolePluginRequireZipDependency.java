package org.homio.api.console.dependency;

import org.homio.api.entity.dependency.DependencyZipInstaller;
import org.homio.api.console.ConsolePlugin;

public interface ConsolePluginRequireZipDependency<T> extends ConsolePlugin<T>, DependencyZipInstaller {

}
