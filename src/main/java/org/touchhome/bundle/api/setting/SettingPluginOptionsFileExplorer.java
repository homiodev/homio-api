package org.touchhome.bundle.api.setting;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface SettingPluginOptionsFileExplorer<T> extends SettingPluginOptionsRemovable<T> {

    @Override
    default SettingType getSettingType() {
        return SettingType.SelectBoxButton;
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

    @Override
    default List<OptionModel> getOptions(EntityContext entityContext) {
        if (levels() > 3) {
            throw new RuntimeException("Unable to scan files more that 3 levels");
        }
        try {
            Path rootPath = rootPath();
            if (rootPath == null) {
                return Collections.emptyList();
            }
            return Files.walk(rootPath, levels())
                    .filter(filterPath())
                    .map(path -> OptionModel.of(String.valueOf(path.toString()), path.getFileName().toString()))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new RuntimeException("Unable to fetch " + TouchHomeUtils.getErrorMessage(ex));
        }
    }

    @Override
    default void removeOption(EntityContext entityContext, String key) throws Exception {
        Files.delete(Paths.get(key));
        entityContext.setting().reloadSettings(getClass());
    }
}
