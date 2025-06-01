package org.homio.api.repository;

import static java.lang.String.format;
import static org.homio.api.fs.archive.ArchiveUtil.UnzipFileIssueHandler.replace;
import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;
import static org.homio.api.util.JsonUtils.YAML_OBJECT_MAPPER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pivovarit.function.ThrowingBiFunction;
import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.homio.api.Context;
import org.homio.api.cache.CachedValue;
import org.homio.api.fs.archive.ArchiveUtil;
import org.homio.api.fs.archive.ArchiveUtil.ArchiveFormat;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.OptionModel;
import org.homio.api.util.CommonUtils;
import org.homio.hquery.Curl;
import org.homio.hquery.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@SuppressWarnings("unused")
@Log4j2
@Accessors(chain = true)
@RequiredArgsConstructor
public class GitHubProject {

  private static final DateTimeFormatter PUBLISHED_AT_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

  private final @NotNull @Getter String repo;
  private final @NotNull @Getter String project;
  private final @NotNull @Getter String api;
  private final @NotNull @Getter Map<String, String> httpHeaders = new HashMap<>();
  private final @NotNull @Getter Path localProjectPath;
  // releases sorted by published_at
  private final CachedValue<List<JsonNode>, GitHubProject> releasesCache =
      new CachedValue<>(
          Duration.ofHours(24),
          gitHubProject ->
              Curl.sendSync(
                  Curl.createGetRequest(gitHubProject.api + "releases", httpHeaders),
                  JsonNode.class,
                  (jsonNode, status) -> {
                    List<JsonNode> releases = new ArrayList<>();
                    try {
                      if (status != HttpStatus.OK.value()) {
                        throw new IllegalStateException(jsonNode.toString());
                      }
                      for (JsonNode node : jsonNode) {
                        releases.add(node);
                      }
                      releases.sort(
                          (o1, o2) -> {
                            try {
                              Instant release1Time =
                                  Instant.from(
                                      PUBLISHED_AT_DATE_FORMAT.parse(
                                          o1.get("published_at").asText()));
                              Instant release2Time =
                                  Instant.from(
                                      PUBLISHED_AT_DATE_FORMAT.parse(
                                          o1.get("published_at").asText()));
                              return Long.compare(
                                  release1Time.toEpochMilli(), release2Time.toEpochMilli());
                            } catch (Exception e) {
                              throw new RuntimeException(e);
                            }
                          });
                    } catch (Exception ex) {
                      log.error(
                          "Unable to fetch releases from GitHub api: {}releases. Error: {}",
                          gitHubProject.api,
                          CommonUtils.getErrorMessage(ex));
                    }
                    return releases;
                  }));

  private final CachedValue<List<JsonNode>, GitHubProject> contentCache =
      new CachedValue<>(
          Duration.ofHours(24),
          gitHubProject ->
              Curl.sendSync(
                  Curl.createGetRequest(gitHubProject.api + "contents", httpHeaders),
                  JsonNode.class,
                  (jsonNode, status) -> {
                    List<JsonNode> contents = new ArrayList<>();
                    try {
                      if (status != HttpStatus.OK.value()) {
                        throw new IllegalStateException(jsonNode.toString());
                      }
                      for (JsonNode node : jsonNode) {
                        contents.add(node);
                      }
                    } catch (Exception ex) {
                      log.error(
                          "Unable to fetch releases from GitHub api: {}releases. Error: {}",
                          gitHubProject.api,
                          CommonUtils.getErrorMessage(ex));
                    }
                    return contents;
                  }));

  @Setter
  @Getter
  @Accessors(chain = true)
  private String linuxExecutableAsset;

  @Setter private @Nullable String installedVersion;

  private @Getter boolean updating;

  @Setter
  private @Nullable ThrowingBiFunction<Context, GitHubProject, String, Exception>
      installedVersionResolver;

  private GitHubProject(
      @NotNull String project, @NotNull String repo, @Nullable Path localProjectPath) {
    this.project = project;
    this.repo = repo;
    this.api = format("https://api.github.com/repos/%s/%s/", project, repo);
    this.localProjectPath =
        localProjectPath == null ? CommonUtils.getInstallPath().resolve(repo) : localProjectPath;
  }

  /**
   * @param repoURL - absolute or relative url
   * @return -
   */
  @SneakyThrows
  public static @NotNull GitHubProject of(@NotNull String repoURL) {
    if (!repoURL.startsWith("https://github.com/")) {
      repoURL = "https://github.com/" + (repoURL.startsWith("/") ? repoURL.substring(1) : repoURL);
    }
    String[] path = new URL(repoURL).getPath().substring(1).split("/");
    return of(path[0], path[1]);
  }

  public static GitHubProject of(@NotNull String project, @NotNull String repo) {
    return of(project, repo, null);
  }

  public static GitHubProject of(
      @NotNull String project, @NotNull String repo, @Nullable Path localProjectPath) {
    return new GitHubProject(project, repo, localProjectPath);
  }

  public static @NotNull List<OptionModel> getReleasesSince(
      @NotNull String version, @NotNull List<OptionModel> versions, boolean includeCurrent) {
    ComparableVersion cv = new ComparableVersion(version);
    return versions.stream()
        .filter(
            v -> {
              String key = v.getKey().toLowerCase();
              key = key.startsWith("v") ? key.substring(1) : key;
              int diff = new ComparableVersion(key).compareTo(cv);
              return diff > 0 || (diff == 0 && includeCurrent);
            })
        .toList();
  }

  private static @NotNull JsonNode findAssetByArchitecture(
      @NotNull Context context, JsonNode release) {
    return context.hardware().findAssetByArchitecture(release);
  }

  @SneakyThrows
  public boolean backup(Path src, Path target) {
    Path srcPath = getLocalProjectPath().resolve(src);
    Path targetPath = getLocalProjectPath().resolve(target);
    if (Files.exists(targetPath)) {
      return false;
    }
    if (Files.isDirectory(srcPath)) {
      FileUtils.copyDirectory(srcPath.toFile(), targetPath.toFile());
    } else {
      Files.copy(srcPath, targetPath);
    }
    return true;
  }

  public boolean isLocalProjectInstalled() {
    return Files.isDirectory(localProjectPath);
  }

  public void installLatestRelease(Context context) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    if (isLocalProjectInstalled()) {
      throw new IllegalStateException("Already installed");
    } else {
      installLatestReleaseInternally(context, future);
    }
  }

  public CompletableFuture<Void> installLatestReleaseAsync(Context context) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    if (isLocalProjectInstalled()) {
      future.completeExceptionally(new IllegalStateException("Already installed"));
    } else {
      context
          .event()
          .runOnceOnInternetUp(
              "wait-download-" + repo, () -> installLatestReleaseInternally(context, future));
    }
    return future;
  }

  @SneakyThrows
  public @Nullable String getInstalledVersion(Context context) {
    if (installedVersion == null) {
      try {
        if (installedVersionResolver != null) {
          installedVersion = installedVersionResolver.apply(context, this);
        } else {
          Path versionPath = localProjectPath.resolve("package.json");
          if (Files.exists(versionPath)) {
            ObjectNode packageNode =
                OBJECT_MAPPER.readValue(Files.readString(versionPath), ObjectNode.class);
            installedVersion = packageNode.get("version").asText();
          }
        }
      } catch (Exception ex) {
        log.error(
            "Unable to fetch project '{}' installed version. Error: {}",
            project,
            CommonUtils.getErrorMessage(ex));
      }
    }
    return installedVersion;
  }

  @SneakyThrows
  public void downloadReleaseAndInstall(
      @NotNull Context context, @NotNull String version, @NotNull ProgressBar progressBar) {
    JsonNode release = getRelease(version);
    JsonNode asset = findAssetByArchitecture(context, release);
    String downloadUrl = asset.get("browser_download_url").asText();
    String extension = CommonUtils.getExtension(downloadUrl);
    log.info(
        "Downloading release: {}. Asset: {}. Download url: {}. Extension: {}",
        version,
        asset,
        downloadUrl,
        extension);
    if (extension.isEmpty()) {
      downloadAndInstallNotArchiveAsset(downloadUrl, progressBar, context);
    } else {
      Path archive = CommonUtils.getTmpPath().resolve(project).resolve(project + "." + extension);
      Curl.downloadWithProgress(downloadUrl, archive, progressBar);
      ArchiveUtil.unzipAndMove(progressBar, archive, localProjectPath);
    }
  }

  public void downloadAndInstallNotArchiveAsset(
      String downloadUrl, @NotNull ProgressBar progressBar, @NotNull Context context) {
    if (linuxExecutableAsset == null) {
      throw new IllegalStateException("Must be implemented by child instance");
    }
    Path targetPath = localProjectPath.resolve(linuxExecutableAsset);
    Curl.downloadWithProgress(downloadUrl, targetPath, progressBar);
    if (SystemUtils.IS_OS_LINUX) {
      context.hardware().execute("chmod +x " + targetPath);
    }
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

  public void setBasicAuthentication(@NotNull String username, @NotNull String password) {
    String auth = username + ":" + password;
    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
    httpHeaders.put(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth));
  }

  /**
   * Download file from GitHub.
   *
   * @param path - relative path
   * @param type - converter type
   * @param <T> -
   * @return -
   */
  @SneakyThrows
  public <T> T getFile(@NotNull String path, @NotNull Class<T> type) {
    String url = format("https://raw.githubusercontent.com/%s/%s/master/%s", project, repo, path);
    if (path.endsWith(".yaml") || path.endsWith(".yml")) {
      return YAML_OBJECT_MAPPER.readValue(new URL(url), type);
    }
    return Curl.sendSync(Curl.createGetRequest(url), type, (t, integer) -> t);
  }

  @SneakyThrows
  public @NotNull Model getPomModel() {
    return getPomModel("pom.xml");
  }

  /**
   * Read pom.xml
   *
   * @param path relative path to pom.xml file
   * @return -
   */
  @SneakyThrows
  public @NotNull Model getPomModel(@NotNull String path) {
    URL url =
        new URL(format("https://raw.githubusercontent.com/%s/%s/master/%s", project, repo, path));
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

  public @Nullable String getVersionReadme(@NotNull String version) {
    JsonNode release = getRelease(version);
    return release.path("body").asText();
  }

  public @NotNull List<OptionModel> getReleasesSince(
      @Nullable String version, boolean includeCurrent) {
    try {
      List<OptionModel> versions =
          releasesCache.getValue(this).stream()
              .map(
                  r ->
                      new Release(
                          r.get("tag_name").asText(),
                          r.get("name").asText(),
                          LocalDateTime.parse(
                              r.get("published_at").asText(),
                              DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")),
                          r.get("prerelease").asBoolean(false)))
              .sorted()
              .map(
                  r -> {
                    OptionModel model = OptionModel.of(r.tagName, r.name);
                    String description =
                        r.created.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    if (r.preRelease) {
                      description += " [pre-release]";
                    }
                    return model.setDescription(description);
                  })
              .collect(Collectors.toList());
      if (version == null) {
        return versions;
      }
      return getReleasesSince(version, versions, includeCurrent);
    } catch (Exception ex) {
      log.error(
          "Unable to fetch release since: {}. Error: {}", version, CommonUtils.getErrorMessage(ex));
      return List.of();
    }
  }

  public @Nullable JsonNode getLastRelease() {
    try {
      List<JsonNode> releases = releasesCache.getValue(this);
      return releases.isEmpty() ? null : releases.get(releases.size() - 1);
    } catch (Exception ignore) {
      return null;
    }
  }

  public @NotNull List<JsonNode> getContent() {
    return contentCache.getValue(this);
  }

  public @NotNull Optional<VersionedFile> getContentFile(String filePrefix) {
    JsonNode jsonNode =
        getContent().stream()
            .filter(s -> s.get("name").asText().startsWith(filePrefix))
            .findAny()
            .orElse(null);
    if (jsonNode != null) {
      String name = jsonNode.get("name").asText();
      VersionedFile versionedFile =
          new VersionedFile(name, jsonNode.get("download_url").asText(), jsonNode);
      int startIndex = name.indexOf("-");
      int endIndex = name.lastIndexOf(".");

      if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
        versionedFile.version = name.substring(startIndex + 1, endIndex);
      }
      return Optional.of(versionedFile);
    }
    return Optional.empty();
  }

  @SneakyThrows
  public void downloadSource(
      @NotNull String name, @NotNull String version, @NotNull Path targetPath) {
    Path tmpPath = CommonUtils.getTmpPath().resolve(name + ".tar.gz");
    Curl.download(api + "tarball/" + version, tmpPath);
    ArchiveUtil.unzip(tmpPath, CommonUtils.getTmpPath(), null, false, null, replace);
    Files.delete(tmpPath);
    Files.move(
        CommonUtils.getTmpPath().resolve(name + "-" + version),
        targetPath,
        StandardCopyOption.REPLACE_EXISTING);
  }

  @SneakyThrows
  public void downloadReleaseFile(
      @NotNull String version,
      @NotNull String asset,
      @NotNull Path archive,
      @NotNull ProgressBar progressBar) {
    String downloadUrl =
        format("https://github.com/%s/%s/releases/download/%s/%s", project, repo, version, asset);
    Curl.downloadWithProgress(downloadUrl, archive, progressBar);
  }

  private void installLatestReleaseInternally(Context context, CompletableFuture<Void> future) {
    log.info("Installing {}/{}", repo, project);
    try {
      String version = getLastReleaseVersion();
      if (version == null) {
        log.error("Unable to find any release from {}/{}", repo, project);
        future.completeExceptionally(
            new RuntimeException("Unable to find release version from: " + repo + "/" + project));
      } else {
        downloadReleaseAndInstall(
            context, version, (progress, message, error) -> log.info(message));
        log.error("Install {}/{} succeeded", repo, project);
        future.complete(null);
      }
    } catch (Exception ex) {
      log.error("Unable to install {}/{}", repo, project, ex);
      future.completeExceptionally(ex);
    }
  }

  private JsonNode getRelease(@NotNull String version) {
    List<JsonNode> releases = releasesCache.getValue(this);
    return releases.stream()
        .filter(r -> r.path("tag_name").asText("").equals(version))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException("Unable to find release: " + version));
  }

  // Helper method to execute some process i.e., download from GitHub, backup, etc...
  public @NotNull ActionResponseModel updateProject(
      @NotNull String name,
      @NotNull ProgressBar progressBar,
      boolean backupProject,
      @NotNull ThrowingFunction<ProjectUpdate, ActionResponseModel, Exception> updateHandler,
      @Nullable ThrowingConsumer<Exception, Exception> onFinally) {
    if (this.updating) {
      return ActionResponseModel.showError("W.ERROR.UPDATE_IN_PROGRESS");
    }
    onFinally = onFinally == null ? e -> {} : onFinally;
    this.installedVersion = null;
    this.updating = true;
    ProjectUpdate projectUpdate = new ProjectUpdate(name, progressBar, this);
    try {
      if (backupProject) {
        projectUpdate.backupProject();
      }
      return updateHandler.apply(projectUpdate);
    } catch (Exception ex) {
      progressBar.progress(
          99,
          format("Error during updating: %s. Error: '%s'", name, CommonUtils.getErrorMessage(ex)));
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

  @AllArgsConstructor
  private static class Release implements Comparable<Release> {

    private String tagName;
    private String name;
    private @NotNull LocalDateTime created;
    private boolean preRelease;

    @Override
    public int compareTo(@NotNull GitHubProject.Release o) {
      return o.created.compareTo(created);
    }
  }

  @Getter
  @RequiredArgsConstructor
  public static class VersionedFile {

    private final String name;
    private final String downloadUrl;
    private final JsonNode rawNode;
    private String version;
  }

  @Getter
  @RequiredArgsConstructor
  public class ProjectUpdate {

    private final @NotNull String name;
    private final @NotNull ProgressBar progressBar;
    private final @NotNull GitHubProject gitHubProject;
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
    public @NotNull ProjectUpdate downloadReleaseFile(
        @NotNull String version, @NotNull String asset, @NotNull Path targetPath) {
      gitHubProject.downloadReleaseFile(version, asset, targetPath, progressBar);
      return this;
    }

    @SneakyThrows
    public @NotNull ProjectUpdate restore(@NotNull Path backupFileOrFolder) {
      progressBar.progress(20, format("Move nodes from backup: '%s'", backupFileOrFolder));
      File targetFileOrDirectory = localProjectPath.resolve(backupFileOrFolder).toFile();
      // remove target node if exists
      FileUtils.deleteDirectory(targetFileOrDirectory);
      FileUtils.moveDirectory(
          CommonUtils.getInstallPath().resolve(backupFileOrFolder + "-backup").toFile(),
          targetFileOrDirectory);
      return this;
    }

    @SneakyThrows
    public @NotNull ProjectUpdate downloadSource(@NotNull String version) {
      progressBar.progress(5, format("Download %s/%s sources of V%s", repo, project, version));
      Path workingPath = CommonUtils.getTmpPath().resolve(project);
      Path archive = workingPath.resolve(name + ".tar.gz");
      Curl.downloadWithProgress(api + "tarball/" + version, archive, progressBar);
      ArchiveUtil.unzipAndMove(progressBar, archive, localProjectPath);
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
        backup =
            CommonUtils.getInstallPath().resolve(localProjectPath.getFileName() + "_backup.zip");
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
        ArchiveUtil.unzip(backup, localProjectPath, null, false, progressBar, replace);
      }
    }
  }
}
