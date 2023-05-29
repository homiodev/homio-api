package org.homio.api.repository;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.JsonNode;
import com.pivovarit.function.ThrowingFunction;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.homio.api.cache.CachedValue;
import org.homio.api.fs.archive.ArchiveUtil;
import org.homio.api.fs.archive.ArchiveUtil.UnzipFileIssueHandler;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.ui.field.ProgressBar;
import org.homio.api.util.CommonUtils;
import org.homio.api.util.Curl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SuppressWarnings("unused")
@Log4j2
@Getter
@RequiredArgsConstructor
public class GitHubProject {

    private static final SimpleDateFormat PUBLISHED_AT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final @NotNull String repo;
    private final @NotNull String project;
    private final @NotNull String api;
    private final @NotNull HttpHeaders httpHeaders = new HttpHeaders();

    private boolean updating;
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
        return new GitHubProject(path[0], path[1]);
    }

    public static GitHubProject of(@NotNull String project, @NotNull String repo) {
        return new GitHubProject(project, repo);
    }

    private GitHubProject(@NotNull String project, @NotNull String repo) {
        this.repo = repo;
        this.project = project;
        this.api = format("https://api.github.com/repos/%s/%s/", project, repo);
    }

    public void setBasicAuthentication(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        httpHeaders.add("Authorization", "Basic " + new String(encodedAuth));
    }

    /**
     * Download file from GitHub.
     * @param path - relative path
     * @param type - converter type
     * @param <T> -
     * @return -
     */
    @SneakyThrows
    public <T> @Nullable T getFile(@NotNull String path, @NotNull Class<T> type) {
        URL url = new URL(format("https://raw.githubusercontent.com/%s/%s/master/%s", project, repo, path));
        if (path.endsWith(".yaml") || path.endsWith(".yml")) {
            return CommonUtils.YAML_OBJECT_MAPPER.readValue(url, type);
        }
        return Curl.restTemplate.getForObject(url.toURI(), type);
    }

    @SneakyThrows
    public Model getPomModel() {
        return getPomModel("pom.xml");
    }

    /**
     * Read pom.xml
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

    // Helper method to execute some process i.e. download from github, backup, etc...
    public @NotNull ActionResponseModel updating(
        @NotNull String name,
        @NotNull Path projectPath,
        @NotNull ProgressBar progressBar,
        @NotNull ThrowingFunction<ProjectUpdate, ActionResponseModel, Exception> updateHandler) {
        if (this.updating) {
            return ActionResponseModel.showError("W.ERROR.UPDATE_IN_PROGRESS");
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

    @Getter
    @RequiredArgsConstructor
    public class ProjectUpdate {

        private final @NotNull String name;
        private final @NotNull Path projectPath;
        private final @NotNull ProgressBar progressBar;

        @SneakyThrows
        public @NotNull ProjectUpdate backup(@NotNull Path backupFileOrFolder) {
            log.info("Backup files{}", backupFileOrFolder);
            FileUtils.copyDirectory(projectPath.resolve(backupFileOrFolder).toFile(),
                CommonUtils.getInstallPath().resolve(backupFileOrFolder + "-backup").toFile());
            return this;
        }

        @SneakyThrows
        public @NotNull ProjectUpdate restore(@NotNull Path backupFileOrFolder) {
            log.info("Restore files {}", backupFileOrFolder);
            FileUtils.deleteDirectory(projectPath.resolve(backupFileOrFolder).toFile());
            FileUtils.moveDirectory(CommonUtils.getInstallPath().resolve(backupFileOrFolder + "-backup").toFile(),
                projectPath.resolve(backupFileOrFolder).toFile());
            return this;
        }

        @SneakyThrows
        public @NotNull ProjectUpdate deleteProject() {
            log.info("Delete project {}", projectPath);
            CommonUtils.deletePath(projectPath);
            return this;
        }

        @SneakyThrows
        public @NotNull ProjectUpdate downloadSource(@NotNull String version) {
            log.info("Download {}/{} sources of V{}", repo, project, version);
            Path targetPath = CommonUtils.getTmpPath().resolve(name + ".tar.gz");
            Curl.downloadWithProgress(api + "tarball/" + version, targetPath, progressBar);
            log.info("Unzip {} sources to {}", targetPath, CommonUtils.getTmpPath());
            List<Path> files = ArchiveUtil.unzip(targetPath, CommonUtils.getTmpPath(), null, false, progressBar,
                UnzipFileIssueHandler.replace);
            Files.delete(targetPath);
            if (!files.isEmpty()) {
                Path unzipFolder = CommonUtils.getTmpPath().relativize(files.iterator().next());
                while (unzipFolder.getParent() != null) {
                    unzipFolder = unzipFolder.getParent();
                }
                CommonUtils.deletePath(projectPath);
                Files.move(CommonUtils.getTmpPath().resolve(unzipFolder), projectPath);
            }
            return this;
        }
    }
}