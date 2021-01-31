package org.touchhome.bundle.api.entity.dependency;

import lombok.SneakyThrows;
import org.apache.logging.log4j.Logger;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.touchhome.bundle.api.util.Curl.downloadWithProgress;

public interface DependencyExecutableInstaller {

    default void installDependency(EntityContext entityContext, String progressKey) throws Exception {
        installDependencyInternal(entityContext, progressKey);
        // check dependency installed
        if (isRequireInstallDependencies(entityContext)) {
            throw new RuntimeException("Something went wrong after install dependency. Executable file still required");
        }
        entityContext.ui().progress(progressKey, 99, "Installing finished");
        afterDependencyInstalled();
    }

    boolean isRequireInstallDependencies(EntityContext entityContext);

    void installDependencyInternal(EntityContext entityContext, String progressKey) throws Exception;

    void afterDependencyInstalled();

    /**
     * Just an utility method
     */
    @SneakyThrows
    default Path downloadAndExtract(String url, String archiveType, String folderName, String progressKey,
                                    EntityContext entityContext, Logger log) {
        log.info("Downloading <{}> from url <{}>", folderName, url);
        Path targetFolder = TouchHomeUtils.getInstallPath().resolve(folderName);
        Path archiveFile = targetFolder.resolve(folderName + "." + archiveType);
        downloadWithProgress(url, archiveFile, progressKey, entityContext);
        entityContext.ui().progress(progressKey, 90, "Unzip files...");
        log.info("Extracting <{}> to path <{}>", archiveFile, targetFolder);
        TouchHomeUtils.unzip(archiveFile, targetFolder, null, entityContext, progressKey);
        Files.deleteIfExists(archiveFile);
        return targetFolder;
    }
}
