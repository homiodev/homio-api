package org.touchhome.bundle.api.setting;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface BundleSettingPluginFileExplorer extends BundleSettingOptionsSettingPlugin<String> {

    @Override
    default Class<String> getType() {
        return String.class;
    }

    @Override
    default SettingType getSettingType() {
        return SettingType.TextSelectBoxDynamic;
    }

    @Override
    default String getIcon() {
        return "fas fa-folder-open";
    }

    Path rootPath();

    // max 3 for now
    default int levels() {
        return 1;
    }

    Predicate<Path> filterPath();

    default boolean removableItems(Path path) {
        return false;
    }

    @Override
    default List<OptionModel> loadAvailableValues(EntityContext entityContext) {
        if (levels() > 3) {
            throw new RuntimeException("Unable to scan files more that 3 levels");
        }
        try {
            return Files.walk(rootPath(), levels())
                    .filter(filterPath())
                    .map(path -> {
                        OptionModel optionModel = OptionModel.key(path.getFileName().toString());
                        if (removableItems(path)) {
                            optionModel.getJson().put("removable", true);
                        }
                        return optionModel;
                    })
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            System.out.println("Unable to fetch " + TouchHomeUtils.getErrorMessage(ex));
        }
        return Collections.emptyList();
    }
}
