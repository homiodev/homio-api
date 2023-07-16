package org.homio.api.entity.dependency;

import java.nio.file.Path;
import org.homio.api.EntityContext;
import org.homio.api.fs.archive.ArchiveUtil;
import org.homio.hquery.Curl;
import org.homio.hquery.ProgressBar;

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
