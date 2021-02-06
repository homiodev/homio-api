package org.touchhome.bundle.api.entity.dependency;

import lombok.SneakyThrows;
import org.apache.logging.log4j.Logger;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.ProgressBar;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.touchhome.bundle.api.util.Curl.downloadWithProgress;

public interface DependencyExecutableInstaller {

    default void installDependency(EntityContext entityContext, ProgressBar progressBar) throws Exception {
        installDependencyInternal(entityContext, progressBar);
        // check dependency installed
        if (isRequireInstallDependencies(entityContext)) {
            throw new RuntimeException("Something went wrong after install dependency. Executable file still required");
        }
        progressBar.progress(99, "Installing finished");
        afterDependencyInstalled();
    }

    boolean isRequireInstallDependencies(EntityContext entityContext);

    void installDependencyInternal(EntityContext entityContext, ProgressBar progressBar) throws Exception;

    void afterDependencyInstalled();

    /**
     * Just an utility method
     */
    @SneakyThrows
    default Path downloadAndExtract(String url, String archiveType, String folderName, ProgressBar progressBar, Logger log) {
        log.info("Downloading <{}> from url <{}>", folderName, url);
        Path targetFolder = TouchHomeUtils.getInstallPath().resolve(folderName);
        Path archiveFile = targetFolder.resolve(folderName + "." + archiveType);
        downloadWithProgress(url, archiveFile, progressBar);
        progressBar.progress(90, "Unzip files...");
        log.info("Extracting <{}> to path <{}>", archiveFile, targetFolder);
        TouchHomeUtils.unzip(archiveFile, targetFolder, null, progressBar);
        Files.deleteIfExists(archiveFile);
        return targetFolder;
    }
}
