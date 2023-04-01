package org.homio.bundle.api.repository;

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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.homio.bundle.api.fs.archive.ArchiveUtil;
import org.homio.bundle.api.model.ActionResponseModel;
import org.homio.bundle.api.model.CachedValue;
import org.homio.bundle.api.ui.field.ProgressBar;
import org.homio.bundle.api.util.Curl;
import org.homio.bundle.api.util.TouchHomeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
                    log.error("Unable to fetch releases from GitHub api: {}/releases", gitHubProject.api, ex);
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
        if (path.endsWith(".yaml")) {
            return TouchHomeUtils.YAML_OBJECT_MAPPER.readValue(url, type);
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
     * @return last release version
     */
    public @Nullable String getLastReleaseVersion() {
        return Optional.ofNullable(getLastRelease()).map(b -> b.path("tag_name").asText()).orElse(null);
    }

    public @NotNull List<String> getReleasesSince(String version) {
        List<String> list = new ArrayList<>();
        boolean foundVersion = false;
        for (JsonNode release : releasesCache.getValue(this)) {
            if (!foundVersion && release.get("tag_name").asText().equals(version)) {
                foundVersion = true;
            }
            if (foundVersion) {
                list.add(release.get("tag_name").asText());
            }
        }
        return list;
    }

    public @Nullable JsonNode getLastRelease() {
        List<JsonNode> releases = releasesCache.getValue(this);
        return releases.isEmpty() ? null : releases.get(releases.size() - 1);
    }

    @SneakyThrows
    public void downloadSource(String name, String version, Path targetPath) {
        Path tmpPath = TouchHomeUtils.getTmpPath().resolve(name + ".tar.gz");
        Curl.download(api + "/tarball/" + version, tmpPath);
        ArchiveUtil.unzip(tmpPath, TouchHomeUtils.getTmpPath(), null, false, null,
                ArchiveUtil.UnzipFileIssueHandler.replace);
        Files.delete(tmpPath);
        Files.move(TouchHomeUtils.getTmpPath().resolve(name + "-" + version),
                targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    // Helper method to execute some process i.e. download from github, backup, etc...
    public @NotNull ActionResponseModel updating(
            @NotNull String name,
            @NotNull Path projectPath,
            @NotNull ProgressBar progressBar,
            @NotNull ThrowingFunction<ProjectUpdate, ActionResponseModel, Exception> updateHandler) {
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

    @Getter
    @RequiredArgsConstructor
    public class ProjectUpdate {
        private final @NotNull String name;
        private final @NotNull Path projectPath;
        private final @NotNull ProgressBar progressBar;

        @SneakyThrows
        public @NotNull ProjectUpdate backup(@NotNull Path backupFileOrFolder) {
            FileUtils.copyDirectory(projectPath.resolve(backupFileOrFolder).toFile(),
                    TouchHomeUtils.getInstallPath().resolve(backupFileOrFolder + "-backup").toFile());
            return this;
        }

        @SneakyThrows
        public @NotNull ProjectUpdate restore(@NotNull Path backupFileOrFolder) {
            FileUtils.deleteDirectory(projectPath.resolve(backupFileOrFolder).toFile());
            FileUtils.moveDirectory(TouchHomeUtils.getInstallPath().resolve(backupFileOrFolder + "-backup").toFile(),
                    projectPath.resolve(backupFileOrFolder).toFile());
            return this;
        }

        @SneakyThrows
        public @NotNull ProjectUpdate deleteProject() {
            FileUtils.deleteDirectory(projectPath.toFile());
            return this;
        }

        @SneakyThrows
        public @NotNull ProjectUpdate downloadSource(@NotNull String version) {
            Path targetPath = TouchHomeUtils.getTmpPath().resolve(name + ".tar.gz");
            Curl.downloadWithProgress(api + "/tarball/" + version, targetPath, progressBar);
            ArchiveUtil.unzip(targetPath, TouchHomeUtils.getTmpPath(), null, false, progressBar,
                    ArchiveUtil.UnzipFileIssueHandler.replace);
            Files.delete(targetPath);
            Files.move(TouchHomeUtils.getTmpPath().resolve(name + "-" + version),
                    projectPath, StandardCopyOption.REPLACE_EXISTING);
            return this;
        }
    }
}
