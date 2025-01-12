package org.homio.api.setting;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.homio.api.Context;
import org.homio.api.model.Icon;
import org.homio.hquery.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface SettingPluginPackageInstall extends SettingPluginButton {

  @Override
  default @Nullable Icon getIcon() {
    return new Icon("fas fa-book");
  }

  @Override
  default @NotNull Class<JSONObject> getType() {
    return JSONObject.class;
  }

  @Override
  default @NotNull SettingType getSettingType() {
    return SettingType.Button;
  }

  PackageContext allPackages(Context context) throws Exception;

  PackageContext installedPackages(Context context) throws Exception;

  void installPackage(Context context, PackageRequest packageRequest, ProgressBar progressBar) throws Exception;

  void unInstallPackage(Context context, PackageRequest packageRequest, ProgressBar progressBar) throws Exception;

  @Override
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
    private boolean readmeLazyLoading;
    private String author;
    private String website;
    private String category;
    private String jarUrl;

    private List<String> versions;

    private String version;
    private Long updated;
    private Long size;

    private boolean removable;

    private Set<String> tags;

    // current status
    private Boolean installing;
    private Boolean removing;
  }

  @Data
  @Accessors(chain = true)
  class PackageRequest {

    private String name;
    private String url;
    private String version;
  }
}
