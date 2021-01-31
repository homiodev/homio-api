package org.touchhome.bundle.api.entity.dependency;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.util.Curl;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.nio.file.Path;

public interface DependencyZipInstaller {

    default void installDependency(EntityContext entityContext, String progressKey) {
        Path targetPath = getRootPath().resolve(dependencyName());
        Curl.downloadWithProgress(getDependencyURL(), targetPath, progressKey, entityContext);
        entityContext.ui().progress(progressKey, 95, "Extracting files...");
        TouchHomeUtils.unzip(targetPath, targetPath.getParent(), null, entityContext, progressKey);
        entityContext.ui().progress(progressKey, 99, "Extracting finished");
        afterDependencyInstalled();
    }

    default boolean requireInstallDependencies() {
        return !TouchHomeUtils.isValidZipArchive(getRootPath().resolve(dependencyName()).toFile());
    }

    void afterDependencyInstalled();

    Path getRootPath();

    String getDependencyURL();

    default String dependencyName() {
        return "dependency.7z";
    }
}
