package org.homio.api.entity.version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import org.homio.api.EntityContext;
import org.homio.api.model.OptionModel;
import org.homio.api.repository.GitHubProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for entities that has specific program to execute and has version
 */
public interface HasGitHubFirmwareVersion extends HasFirmwareVersion {

    default @Nullable String getFirmwareVersion() {
        return getGitHubProject().getInstalledVersion(getEntityContext());
    }

    /**
     * Return last available firmware version if entity able to update to it
     *
     * @return last available version
     */
    default @Nullable String getLastFirmwareVersion() {
        return getGitHubProject().getLastReleaseVersion();
    }

    default @Nullable String getFirmwareVersionReadme(@NotNull String version) {
        return getGitHubProject().getVersionReadme(version);
    }

    default @Nullable List<OptionModel> getNewAvailableVersion() {
        GitHubProject gitHubProject = getGitHubProject();
        String installedVersion = gitHubProject.getInstalledVersion(getEntityContext());
        List<String> releases = gitHubProject.getReleasesSince(installedVersion, false);
        return OptionModel.list(releases);
    }

    default boolean isFirmwareUpdating() {
        return getGitHubProject().isUpdating();
    }

    @JsonIgnore
    @NotNull GitHubProject getGitHubProject();

    @JsonIgnore
    EntityContext getEntityContext();
}
