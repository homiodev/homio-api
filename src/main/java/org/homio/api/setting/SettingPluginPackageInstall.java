package org.homio.api.setting;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.*;
import lombok.experimental.Accessors;
import org.homio.api.Context;
import org.homio.api.model.Icon;
import org.homio.hquery.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface SettingPluginPackageInstall extends SettingPluginButton {

    String BUILT_IN_TAG = "Built-in";

    @Override
    default @Nullable Icon getIcon() {
        return new Icon("fas fa-book");
    }

    PackageContext allPackages(@NotNull Context context) throws Exception;

    PackageContext installedPackages(@NotNull Context context) throws Exception;

    void installPackage(@NotNull Context context, @NotNull PackageRequest packageRequest,
                        @NotNull ProgressBar progressBar) throws Exception;

    void unInstallPackage(@NotNull Context context, @NotNull PackageRequest packageRequest,
                          @NotNull ProgressBar progressBar) throws Exception;

    @Override
    @Nullable
    default String getConfirmMsg() {
        return null;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    class PackageContext {

        String error;
        Collection<PackageModel> packages;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    class PackageModel {

        private String name;
        private String title;
        private String description;

        private String icon;
        private String readme;
        private Boolean readmeLazyLoading;
        private String author;
        private String website;
        private String category;
        private String jarUrl;

        private List<String> versions;

        private String version;
        private Long updated;
        private Long size;

        private Boolean disableRemovable;

        private Set<String> tags;

        // current status
        private Boolean installing;
        private Boolean removing;

        // last release date
        private Long lastUpdated;

        private boolean disabled; // in case if unable to install package for any reason
    }

    @Data
    @Accessors(chain = true)
    class PackageRequest {

        private String name;
        private String url;
        private String version;
    }
}
