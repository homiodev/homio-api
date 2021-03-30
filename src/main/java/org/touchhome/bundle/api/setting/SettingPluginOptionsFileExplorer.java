package org.touchhome.bundle.api.setting;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.field.UIFieldType;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public interface SettingPluginOptionsFileExplorer extends SettingPluginOptionsRemovable<Path> {

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.TextSelectBoxDynamic;
    }

    default String getIcon() {
        return "fas fa-folder-open";
    }

    Path rootPath();

    // max 3 for now
    default int levels() {
        return 1;
    }

    default boolean visitFile(Path path, BasicFileAttributes attrs) {
        if (Files.exists(path) && Files.isReadable(path)) {
            String name = path.getFileName() == null ? path.toString() : path.getFileName().toString();
            return !name.startsWith("$") && !name.startsWith(".");
        }
        return false;
    }

    default boolean visitDirectory(Path dir, BasicFileAttributes attrs) {
        if (Files.isReadable(dir)) {
            String name = dir.getFileName() == null ? dir.toString() : dir.getFileName().toString();
            return !name.startsWith("$") && !name.startsWith(".");
        }
        return false;
    }

    default boolean pushDirectory(Path dir) {
        return true;
    }

    @Override
    default Class<Path> getType() {
        return Path.class;
    }

    @Override
    default Collection<OptionModel> getOptions(EntityContext entityContext, JSONObject params) {
        if (levels() > 3) {
            throw new RuntimeException("Unable to scan files more that 3 levels");
        }
        return getFilePath(false, params == null || !params.has("param0") ? null : Paths.get(params.getString("param0")));
    }

    default List<OptionModel> getFilePath(boolean includePath, Path rootPath) {
        try {
            final Path root = rootPath == null ? rootPath() : rootPath;
            if (root == null) {
                return Collections.emptyList();
            }
            Map<Path, OptionModel> fs = new HashMap<>();
            Files.walkFileTree(root,
                    new HashSet<>(Collections.singletonList(FileVisitOption.FOLLOW_LINKS)),
                    Math.max(levels(), 1), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                            if (SettingPluginOptionsFileExplorer.this.visitFile(path, attrs)) {
                                handlePath(path);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                            if (visitDirectory(dir, attrs)) {
                                if (pushDirectory(dir)) {
                                    handlePath(dir);
                                }
                                return FileVisitResult.CONTINUE;
                            }
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException e) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        private void handlePath(Path path) {
                            Path parent = path.getParent();
                            OptionModel model = createOptionModelFromPath(path);
                            if (!flatStructure() && parent != null && fs.containsKey(parent)) {
                                fs.get(parent).addChild(model);
                            }
                            fs.put(path, model);
                        }

                        @SneakyThrows
                        private OptionModel createOptionModelFromPath(Path path) {
                            String key = path.getFileName() == null ? path.toString() : path.getFileName().toString();
                            if (skipRootInTreeStructure() && path.equals(root)) {
                                key = null;
                            }
                            OptionModel model = OptionModel.of(key);
                            boolean isDirectory = Files.isDirectory(path);
                            if (includePath || (allowRequestNextLevel() && isDirectory)) {
                                model.getJson().put("path", path.toString());
                                if (isDirectory) {
                                    if (Files.list(path).findAny().isPresent()) {
                                        model.getJson().put("dir", true);
                                    } else {
                                        model.getJson().put("emptyDir", true);
                                    }
                                }
                            }
                            return model;
                        }
                    });
            List<OptionModel> result = new ArrayList<>(flatStructure() ? fs.values() : fs.containsKey(root) ?
                    fs.get(root).getOrCreateChildren() : Collections.emptyList());
            if (allowRequestNextLevel()) {
                handleRequestNextPath(result);
            }
            result.sort(pathComparator());
            return result;
        } catch (Exception ex) {
            throw new RuntimeException("Unable to fetch " + TouchHomeUtils.getErrorMessage(ex));
        }
    }

    default void handleRequestNextPath(Collection<OptionModel> result) {
        for (OptionModel model : result) {
            if (model.getChildren() == null) {
                if (model.getJson().has("dir")) {
                    model.getJson().put("requestNext", true);
                }
            } else {
                handleRequestNextPath(model.getChildren());
            }
        }
    }

    default boolean skipRootInTreeStructure() {
        return true;
    }

    default boolean flatStructure() {
        return false;
    }

    default boolean allowRequestNextLevel() {
        return false;
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
        List<OptionModel> optionModels = getFilePath(true, null);
        for (OptionModel optionModel : optionModels) {
            OptionModel foundModel = optionModel.findByKey(value);
            if (foundModel != null) {
                return foundModel;
            }
        }

        return null;
    }
}
