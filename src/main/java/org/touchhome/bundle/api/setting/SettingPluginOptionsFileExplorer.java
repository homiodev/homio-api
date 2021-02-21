package org.touchhome.bundle.api.setting;

import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface SettingPluginOptionsFileExplorer extends SettingPluginOptionsRemovable<Path> {

    @Override
    default SettingType getSettingType() {
        return SettingType.SelectBoxButton;
    }

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
    default Class<Path> getType() {
        return Path.class;
    }

    @Override
    default List<OptionModel> getOptions(EntityContext entityContext) {
        if (levels() > 3) {
            throw new RuntimeException("Unable to scan files more that 3 levels");
        }
        return getFilePath(false);
    }

    default List<OptionModel> getFilePath(boolean includePath) {
        try {
            Path rootPath = rootPath();
            if (rootPath == null) {
                return Collections.emptyList();
            }
            return Files.walk(rootPath, levels())
                    .filter(filterPath())
                    .map(path -> {
                        OptionModel model = OptionModel.key(path.getFileName().toString());
                        if (includePath) {
                            model.json(jsonObject -> jsonObject.put("path", path.toString()));
                        }
                        return model;
                    })
                    .sorted(pathComparator())
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new RuntimeException("Unable to fetch " + TouchHomeUtils.getErrorMessage(ex));
        }
    }

    default Comparator<OptionModel> pathComparator() {
        return Comparator.comparing(OptionModel::getTitleOrKey);
    }

    @Override
    default void removeOption(EntityContext entityContext, String value) throws Exception {
        Path path = parseValue(entityContext, value);
        Files.delete(path);
        entityContext.setting().reloadSettings(getClass());
    }

    @Override
    default Path parseValue(EntityContext entityContext, String value) {
        OptionModel foundModel = findOptionModelByKey(value);
        String path = foundModel == null ? value : foundModel.getJson().getString("path");
        return StringUtils.isEmpty(path) ? null : Paths.get(path);
    }

    default OptionModel findOptionModelByKey(String value) {
        List<OptionModel> optionModels = getFilePath(true);
        for (OptionModel optionModel : optionModels) {
            OptionModel foundModel = optionModel.findByKey(value);
            if (foundModel != null) {
                return foundModel;
            }
        }

        return null;
    }
}
