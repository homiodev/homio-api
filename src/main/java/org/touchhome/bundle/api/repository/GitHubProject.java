package org.touchhome.bundle.api.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pivovarit.function.ThrowingFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.touchhome.bundle.api.fs.archive.ArchiveUtil;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.ui.field.ProgressBar;
import org.touchhome.bundle.api.util.Curl;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Log4j2
@Getter
@RequiredArgsConstructor
public class GitHubProject {
    private final String repo;
    private final String project;
    private final String api;

    private Map<String, Object> lastRelease;
    private long lastReleaseCheck;
    private boolean updating;

    public GitHubProject(@NotNull String repo, @NotNull String project) {
        this.repo = repo;
        this.project = project;
        this.api = format("https://api.github.com/repos/%s/%s/", repo, project);
    }

    public ActionResponseModel updating(String name, Path projectPath, ProgressBar progressBar,
                                        ThrowingFunction<ProjectUpdate, ActionResponseModel, Exception> updateHandler) {
        if (this.updating) {
            return ActionResponseModel.showError("UPDATE_IN_PROGRESS");
        }
        this.updating = true;
        ProjectUpdate projectUpdate = new ProjectUpdate(name, projectPath, progressBar);
        try {
            return updateHandler.apply(projectUpdate);
        } catch (Exception ex) {
            log.error("Error during installing app", ex);
            return ActionResponseModel.showError(ex);
        } finally {
            updating = false;
        }
    }

    public String getLastReleaseVersion() {
        return Optional.ofNullable(getLastRelease()).map(b -> b.get("tag_name").toString()).orElse(null);
    }

    public List<String> getTags() {
        ArrayNode tags = Curl.get(api + "tags", ArrayNode.class);
        List<String> result = new ArrayList<>(tags.size());
        for (JsonNode tag : tags) {
            result.add(tag.get("name").asText());
        }
        result.sort(Collections.reverseOrder());
        return result;
    }

    public List<String> getTagsSince(String version) {
        List<String> tags = getTags();
        int vi = tags.indexOf(version);
        return vi == -1 ? tags : tags.subList(0, vi);
    }

    public Map<String, Object> getLastRelease() {
        if (lastRelease == null || TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastReleaseCheck) >= 24) {
            lastReleaseCheck = System.currentTimeMillis();
            try {
                lastRelease = Curl.get(api + "releases/latest", Map.class);
            } catch (Exception ex) {
                return null;
            }
        }
        return lastRelease;
    }

    @Getter
    @RequiredArgsConstructor
    public class ProjectUpdate {
        private final String name;
        private final Path projectPath;
        private final ProgressBar progressBar;

        @SneakyThrows
        public ProjectUpdate backup(Path backupFileOrFolder) {
            FileUtils.copyDirectory(projectPath.resolve(backupFileOrFolder).toFile(),
                    TouchHomeUtils.getInstallPath().resolve(backupFileOrFolder + "-backup").toFile());
            return this;
        }

        @SneakyThrows
        public ProjectUpdate restore(Path backupFileOrFolder) {
            FileUtils.deleteDirectory(projectPath.resolve(backupFileOrFolder).toFile());
            FileUtils.moveDirectory(TouchHomeUtils.getInstallPath().resolve(backupFileOrFolder + "-backup").toFile(),
                    projectPath.resolve(backupFileOrFolder).toFile());
            return this;
        }

        @SneakyThrows
        public ProjectUpdate deleteProject() {
            FileUtils.deleteDirectory(projectPath.toFile());
            return this;
        }

        @SneakyThrows
        public ProjectUpdate download(String version) {
            Path targetPath = TouchHomeUtils.getInstallPath().resolve(name + ".tar.gz");
            Curl.downloadWithProgress(api + "/tarball/" + version, targetPath, progressBar);
            ArchiveUtil.unzip(targetPath, TouchHomeUtils.getInstallPath(), null, false, progressBar,
                    ArchiveUtil.UnzipFileIssueHandler.replace);
            Files.delete(targetPath);
            Files.move(TouchHomeUtils.getInstallPath().resolve("zigbee2mqtt-" + version),
                    projectPath, StandardCopyOption.REPLACE_EXISTING);
            return this;
        }
    }
}
