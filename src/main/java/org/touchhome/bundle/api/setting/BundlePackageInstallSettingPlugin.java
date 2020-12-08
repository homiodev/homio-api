package org.touchhome.bundle.api.setting;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface BundlePackageInstallSettingPlugin extends BundleSettingPlugin<JSONObject> {

    @Override
    default String getIcon() {
        return "fas fa-book";
    }

    @Override
    default boolean transientState() {
        return true;
    }

    @Override
    default Class<JSONObject> getType() {
        return JSONObject.class;
    }

    @Override
    default SettingType getSettingType() {
        return SettingType.Button;
    }

    PackageContext allPackages(EntityContext entityContext) throws Exception;

    PackageContext installedPackages(EntityContext entityContext) throws Exception;

    void installPackage(EntityContext entityContext, PackageRequest packageRequest, String progressKey) throws Exception;

    void unInstallPackage(EntityContext entityContext, PackageRequest packageRequest, String progressKey) throws Exception;

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
        private String image;
        private String readme;
        private String author;
        private String website;
        private String category;

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

    @Getter
    @Setter
    class PackageRequest {
        private String name;
        private String url;
        private String version;
    }
}
