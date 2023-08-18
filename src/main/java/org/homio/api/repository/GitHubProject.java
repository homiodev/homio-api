package org.homio.api.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import com.pivovarit.function.ThrowingSupplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.homio.api.cache.CachedValue;
import org.homio.api.fs.archive.ArchiveUtil;
import org.homio.api.fs.archive.ArchiveUtil.ArchiveFormat;
import org.homio.api.fs.archive.ArchiveUtil.UnzipFileIssueHandler;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.util.CommonUtils;
import org.homio.hquery.Curl;
import org.homio.hquery.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.*;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;
import static org.homio.api.util.JsonUtils.YAML_OBJECT_MAPPER;

@SuppressWarnings("unused")
@Log4j2
@Getter
@Accessors(chain = true)
@RequiredArgsConstructor
public class GitHubProject {

    private static final SimpleDateFormat PUBLISHED_AT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final @NotNull String repo;
    private final @NotNull String project;
    private final @NotNull String api;
    private final @NotNull HttpHeaders httpHeaders = new HttpHeaders();
    private final @NotNull Path localProjectPath;
    // releases sorted by published_at
    private final CachedValue<List<JsonNode>, GitHubProject> releasesCache =
            new CachedValue<>(Duration.ofHours(24), gitHubProject -> {
                List<JsonNode> releases = new ArrayList<>();
                try {
                    ResponseEntity<JsonNode> responseEntity = Curl.restTemplate.exchange(gitHubProject.api + "releases",
                            HttpMethod.GET, new HttpEntity<JsonNode>(httpHeaders), JsonNode.class);
                    if (responseEntity.getStatusCode() != HttpStatus.OK) {
                        throw new IllegalStateException(responseEntity.toString());
                    }
                    for (JsonNode node : Objects.requireNonNull(responseEntity.getBody())) {
                        if (!node.get("prerelease").asBoolean(true)) {
                            releases.add(node);
                        }
                    }
                    releases.sort((o1, o2) -> {
                        try {
                            return Long.compare(PUBLISHED_AT_DATE_FORMAT.parse(o1.get("published_at").asText()).getTime(),
                                    PUBLISHED_AT_DATE_FORMAT.parse(o2.get("published_at").asText()).getTime());
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    return releases;
                } catch (Exception ex) {
                    log.error("Unable to fetch releases from GitHub api: {}releases. Error: {}", gitHubProject.api, CommonUtils.getErrorMessage(ex));
                }
                return releases;
            });
    @Setter
    private @Nullable String installedVersion;
    private boolean updating;
    @Setter
    private @Nullable ThrowingSupplier<String, Exception> installedVersionResolver;

    private GitHubProject(@NotNull String project, @NotNull String repo, @Nullable Path localProjectPath) {
        this.project = project;
        this.repo = repo;
        this.api = format("https://api.github.com/repos/%s/%s/", project, repo);
        this.localProjectPath = localProjectPath == null ? CommonUtils.getTmpPath().resolve(project) : localProjectPath;
    }

    /**
     * @param repoURL - absolute or relative url
     * @return -
     */
    @SneakyThrows
    public static GitHubProject of(@NotNull String repoURL) {
        if (!repoURL.startsWith("https://github.com/")) {
            repoURL = "https://github.com/" + (repoURL.startsWith("/") ? repoURL.substring(1) : repoURL);
        }
        String[] path = new URL(repoURL).getPath().substring(1).split("/");
        return of(path[0], path[1]);
    }

    public static GitHubProject of(@NotNull String project, @NotNull String repo) {
        return of(project, repo, null);
    }

    public static GitHubProject of(@NotNull String project, @NotNull String repo, @Nullable Path localProjectPath) {
        return new GitHubProject(project, repo, localProjectPath);
    }

    public static @NotNull List<String> getReleasesSince(String version, List<String> versions, boolean includeCurrent) {
        int versionIndex = versions.indexOf(version);
        if (versionIndex >= 0) {
            if (includeCurrent) {
                return versions.subList(versionIndex, versions.size());
            }
            return versions.subList(versionIndex + 1, versions.size());
        }
        return versions;
    }

    @SneakyThrows
    public @Nullable String getInstalledVersion() {
        if (installedVersion == null) {
            try {
                if (installedVersionResolver != null) {
                    installedVersion = installedVersionResolver.get();
                } else {
                    Path versionPath = localProjectPath.resolve("package.json");
                    if (Files.exists(versionPath)) {
                        ObjectNode packageNode = OBJECT_MAPPER.readValue(Files.readString(versionPath), ObjectNode.class);
                        installedVersion = packageNode.get("version").asText();
                    }
                }
            } catch (Exception ex) {
                log.error("Unable to fetch project '{}' installed version. Error: {}", project, CommonUtils.getErrorMessage(ex));
            }
        }
        return installedVersion;
    }

    @SneakyThrows
    public void deleteProject() {
        installedVersion = null;
        log.info("Delete project: '{}'", localProjectPath);
        try {
            if (Files.exists(localProjectPath)) {
                if (Files.isDirectory(localProjectPath)) {
                    FileUtils.deleteDirectory(localProjectPath.toFile());
                } else {
                    Files.delete(localProjectPath);
                }
            }
        } catch (Exception ex) {
            log.error("Unable to delete project: '{}'", localProjectPath);
            throw ex;
        }
    }

    public void setBasicAuthentication(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        httpHeaders.add("Authorization", "Basic " + new String(encodedAuth));
    }

    /**
     * Download file from GitHub.
     *
     * @param path - relative path
     * @param type - converter type
     * @param <T>  -
     * @return -
     */
    @SneakyThrows
    public <T> @Nullable T getFile(@NotNull String path, @NotNull Class<T> type) {
        URL url = new URL(format("https://raw.githubusercontent.com/%s/%s/master/%s", project, repo, path));
        if (path.endsWith(".yaml") || path.endsWith(".yml")) {
            return YAML_OBJECT_MAPPER.readValue(url, type);
        }
        return Curl.restTemplate.getForObject(url.toURI(), type);
    }

    @SneakyThrows
    public Model getPomModel() {
        return getPomModel("pom.xml");
    }

    /**
     * Read pom.xml
     *
     * @param path relative path to pom.xml file
     * @return -
     */
    @SneakyThrows
    public Model getPomModel(@NotNull String path) {
        URL url = new URL(format("https://raw.githubusercontent.com/%s/%s/master/%s", project, repo, path));
        return new MavenXpp3Reader().read(url.openStream());
    }

    /**
     * Get project last released version
     *
     * @return last release version
     */
    public @Nullable String getLastReleaseVersion() {
        return Optional.ofNullable(getLastRelease()).map(b -> b.path("tag_name").asText()).orElse(null);
    }

    public @NotNull List<String> getReleasesSince(String version, boolean includeCurrent) {
        List<String> versions = releasesCache.getValue(this)
                .stream()
                .map(r -> r.get("tag_name").asText())
                .collect(Collectors.toList());
        return getReleasesSince(version, versions, includeCurrent);
    }

    public @Nullable JsonNode getLastRelease() {
        List<JsonNode> releases = releasesCache.getValue(this);
        return releases.isEmpty() ? null : releases.get(releases.size() - 1);
    }

    @SneakyThrows
    public void downloadSource(String name, String version, Path targetPath) {
        Path tmpPath = CommonUtils.getTmpPath().resolve(name + ".tar.gz");
        Curl.download(api + "tarball/" + version, tmpPath);
        ArchiveUtil.unzip(tmpPath, CommonUtils.getTmpPath(), null, false, null,
                ArchiveUtil.UnzipFileIssueHandler.replace);
        Files.delete(tmpPath);
        Files.move(CommonUtils.getTmpPath().resolve(name + "-" + version),
                targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @SneakyThrows
    public void downloadReleaseFile(@NotNull String version, @NotNull String asset,
                                    @NotNull Path targetPath, @NotNull ProgressBar progressBar) {
        String downloadUrl = format("https://github.com/%s/%s/releases/download/%s/%s", project, repo, version, asset);
        Curl.downloadWithProgress(downloadUrl, targetPath, progressBar);
    }

    // Helper method to execute some process i.e. download from github, backup, etc...
    public @NotNull ActionResponseModel updateProject(
            @NotNull String name,
            @NotNull ProgressBar progressBar,
            boolean backupProject,
            @NotNull ThrowingFunction<ProjectUpdate, ActionResponseModel, Exception> updateHandler,
            @Nullable ThrowingConsumer<Exception, Exception> onFinally) {
        if (this.updating) {
            return ActionResponseModel.showError("W.ERROR.UPDATE_IN_PROGRESS");
        }
        onFinally = onFinally == null ? e -> {
        } : onFinally;
        this.installedVersion = null;
        this.updating = true;
        ProjectUpdate projectUpdate = new ProjectUpdate(name, progressBar, this);
        try {
            if (backupProject) {
                projectUpdate.backupProject();
            }
            return updateHandler.apply(projectUpdate);
        } catch (Exception ex) {
            progressBar.progress(99, format("Error during updating: %s. Error: '%s'", name, CommonUtils.getErrorMessage(ex)));
            if (backupProject) {
                projectUpdate.restoreProject();
            }
            try {
                onFinally.accept(ex);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return ActionResponseModel.showError(ex);
        } finally {
            updating = false;
            projectUpdate.finish();
            try {
                onFinally.accept(null);
            } catch (Exception ignore) {
            }
        }
    }

    @Getter
    @RequiredArgsConstructor
    public class ProjectUpdate {

        private final @NotNull String name;
        private final @NotNull ProgressBar progressBar;
        private final @NotNull GitHubProject project;
        private @Nullable Path backup;

        public boolean isHasBackup() {
            return backup != null;
        }

        @SneakyThrows
        public @NotNull ProjectUpdate copyFromBackup(Set<Path> nodes) {
            if (backup == null) {
                throw new IllegalArgumentException("Backup is null for project: " + name);
            }
            progressBar.progress(20, format("Restore project '%s' backup nodes: '%s'", name, nodes));
            ArchiveUtil.copyEntries(backup, nodes, localProjectPath, false);
            return this;
        }

        @SneakyThrows
        public @NotNull ProjectUpdate downloadReleaseFile(@NotNull String version, @NotNull String asset, @NotNull Path targetPath) {
            project.downloadReleaseFile(version, asset, targetPath, progressBar);
            return this;
        }

        @SneakyThrows
        public @NotNull ProjectUpdate restore(@NotNull Path backupFileOrFolder) {
            progressBar.progress(20, format("Move nodes from backup: '%s'", backupFileOrFolder));
            File targetFileOrDirectory = localProjectPath.resolve(backupFileOrFolder).toFile();
            // remove target node if exists
            FileUtils.deleteDirectory(targetFileOrDirectory);
            FileUtils.moveDirectory(CommonUtils.getInstallPath().resolve(backupFileOrFolder + "-backup").toFile(),
                    targetFileOrDirectory);
            return this;
        }

        @SneakyThrows
        public @NotNull ProjectUpdate downloadSource(@NotNull String version) {
            progressBar.progress(5, format("Download %s/%s sources of V%s", repo, project, version));
            Path targetPath = CommonUtils.getTmpPath().resolve(name + ".tar.gz");
            Curl.downloadWithProgress(api + "tarball/" + version, targetPath, progressBar);
            progressBar.progress(10, format("Unzip %s sources to %s", targetPath, CommonUtils.getTmpPath()));
            List<Path> files = ArchiveUtil.unzip(targetPath, CommonUtils.getTmpPath(), null, false, progressBar,
                    UnzipFileIssueHandler.replace);
            Files.delete(targetPath);
            if (!files.isEmpty()) {
                Path unzipFolder = CommonUtils.getTmpPath().relativize(files.iterator().next());
                while (unzipFolder.getParent() != null) {
                    unzipFolder = unzipFolder.getParent();
                }
                CommonUtils.deletePath(localProjectPath);
                Files.move(CommonUtils.getTmpPath().resolve(unzipFolder), localProjectPath);
            }
            return this;
        }

        @SneakyThrows
        public void finish() {
            if (backup != null) {
                Files.delete(backup);
                backup = null;
            }
        }

        private void backupProject() {
            progressBar.progress(20, format("Backup project: '%s'", name));
            if (Files.exists(localProjectPath)) {
                backup = CommonUtils.getInstallPath().resolve(localProjectPath.getFileName() + "_backup.zip");
                ArchiveUtil.zip(localProjectPath, backup, ArchiveFormat.zip, progressBar, false);
            }
        }

        @SneakyThrows
        private void restoreProject() {
            if (backup != null) {
                progressBar.progress(80, format("Restore project '%s'", name));
                if (Files.exists(localProjectPath)) {
                    CommonUtils.deletePath(localProjectPath);
                }
                ArchiveUtil.unzip(backup, localProjectPath, null, false, progressBar, UnzipFileIssueHandler.replace);
                backup = null;
            }
        }
    }
}
