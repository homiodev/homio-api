package org.touchhome.bundle.api.entity.dependency;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.fs.archive.ArchiveUtil;
import org.touchhome.bundle.api.ui.field.ProgressBar;
import org.touchhome.bundle.api.util.Curl;

import java.nio.file.Path;

public interface DependencyZipInstaller {

    default void installDependency(EntityContext entityContext, ProgressBar progressBar) {
        Path targetPath = getRootPath().resolve(dependencyName());
        Curl.downloadWithProgress(getDependencyURL(), targetPath, progressBar);
        progressBar.progress(95, "Extracting files...");
        ArchiveUtil.unzip(targetPath, targetPath.getParent(), null, false, progressBar,
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
