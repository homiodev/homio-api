package org.homio.api.entity.version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.homio.api.Context;
import org.homio.api.model.OptionModel;
import org.homio.api.repository.GitHubProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface for entities that has specific program to execute and has version
 */
public interface HasGitHubFirmwareVersion extends HasFirmwareVersion {

  default @Nullable String getFirmwareVersion() {
    return getGitHubProject().getInstalledVersion(context());
  }

  /**
   * Return last available firmware version if entity able to update to it
   *
   * @return last available version
   */
  default @Nullable String getLastFirmwareVersion() {
    try {
      return getGitHubProject().getLastReleaseVersion();
    } catch (Exception ignore) {
      return null;
    }
  }

  default @Nullable String getFirmwareVersionReadme(@NotNull String version) {
    return getGitHubProject().getVersionReadme(version);
  }

  default @Nullable List<OptionModel> getNewAvailableVersion() {
    GitHubProject gitHubProject = getGitHubProject();
    String installedVersion = gitHubProject.getInstalledVersion(context());
    return gitHubProject.getReleasesSince(installedVersion, false);
  }

  default boolean isFirmwareUpdating() {
    return getGitHubProject().isUpdating();
  }

  @JsonIgnore
  @NotNull GitHubProject getGitHubProject();

  @JsonIgnore
  @NotNull Context context();
}
