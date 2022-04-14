package org.touchhome.bundle.api.entity.dependency;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.common.model.ProgressBar;
import org.touchhome.common.util.CommonUtils;
import org.touchhome.common.util.Curl;

import java.nio.file.Path;

public interface DependencyZipInstaller {

    default void installDependency(EntityContext entityContext, ProgressBar progressBar) {
        Path targetPath = getRootPath().resolve(dependencyName());
        Curl.downloadWithProgress(getDependencyURL(), targetPath, progressBar);
        progressBar.progress(95, "Extracting files...");
        CommonUtils.unzip(targetPath, targetPath.getParent(), null, progressBar);
        progressBar.progress(99, "Extracting finished");
        afterDependencyInstalled();
    }

    default boolean requireInstallDependencies() {
        return !CommonUtils.isValidZipArchive(getRootPath().resolve(dependencyName()).toFile());
    }

    void afterDependencyInstalled();

    Path getRootPath();

    String getDependencyURL();

    default String dependencyName() {
        return "dependency.7z";
    }
}
