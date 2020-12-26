package org.touchhome.bundle.api.console.dependency;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.lang3.SystemUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.console.ConsolePlugin;
import org.touchhome.bundle.api.util.Curl;

import java.nio.file.Files;
import java.nio.file.Path;

public interface ConsolePluginRequireZipDependency<T> extends ConsolePlugin<T> {

    default void installDependency(EntityContext entityContext, String progressKey) throws Exception {
        String osName = SystemUtils.IS_OS_LINUX ? "linux" : "win";
        Path targetPath = getRootPath().resolve(dependencyName());
        Curl.downloadWithProgress(getDependencyURL(osName), targetPath, progressKey, entityContext);
        entityContext.ui().progress(progressKey, 95, "Extracting files...");
        ZipFile zipFile = new ZipFile(targetPath.toFile());
        zipFile.extractAll(targetPath.getParent().toString());
        entityContext.ui().progress(progressKey, 99, "Extracting finished");
        afterDependencyInstalled();
    }

    default boolean requireInstallDependencies() {
        // check file existence
        if (!Files.exists(getRootPath().resolve(dependencyName()))) {
            return true;
        }
        // check if zip valid
        if (dependencyName().endsWith(".zip")) {
            ZipFile zipFile = new ZipFile(getRootPath().resolve(dependencyName()).toFile());
            if (!zipFile.isValidZipFile()) {
                return true;
            }
        }
        return false;
    }

    void afterDependencyInstalled();

    Path getRootPath();

    String getDependencyURL(String osName);

    default String dependencyName() {
        return "dependency.zip";
    }
}
