package org.touchhome.bundle.api.entity.dependency;

import java.nio.file.Path;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.common.model.ProgressBar;
import org.touchhome.common.util.ArchiveUtil;
import org.touchhome.common.util.Curl;

public interface DependencyZipInstaller {

    default void installDependency(EntityContext entityContext, ProgressBar progressBar) {
        Path targetPath = getRootPath().resolve(dependencyName());
        Curl.downloadWithProgress(getDependencyURL(), targetPath, progressBar);
        progressBar.progress(95, "Extracting files...");
        ArchiveUtil.unzip(
                targetPath,
                targetPath.getParent(),
                null,
                false,
                progressBar,
                ArchiveUtil.UnzipFileIssueHandler.replace);
        progressBar.progress(99, "Extracting finished");
        afterDependencyInstalled();
    }

    default boolean requireInstallDependencies() {
        return !ArchiveUtil.isValidArchive(getRootPath().resolve(dependencyName()));
    }

    void afterDependencyInstalled();

    Path getRootPath();

    String getDependencyURL();

    default String dependencyName() {
        return "dependency.7z";
    }
}
