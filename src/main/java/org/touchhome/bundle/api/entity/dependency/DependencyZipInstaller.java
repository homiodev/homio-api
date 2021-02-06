package org.touchhome.bundle.api.entity.dependency;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.ProgressBar;
import org.touchhome.bundle.api.util.Curl;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.nio.file.Path;

public interface DependencyZipInstaller {

    default void installDependency(EntityContext entityContext, ProgressBar progressBar) {
        Path targetPath = getRootPath().resolve(dependencyName());
        Curl.downloadWithProgress(getDependencyURL(), targetPath, progressBar);
        progressBar.progress(95, "Extracting files...");
        TouchHomeUtils.unzip(targetPath, targetPath.getParent(), null, progressBar);
        progressBar.progress(99, "Extracting finished");
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
