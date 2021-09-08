package org.touchhome.bundle.api.setting;

import lombok.SneakyThrows;
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

    /**
     * Write file to UI
     */
    default boolean writeFile(Path path, BasicFileAttributes attrs) {
        if (Files.exists(path) && Files.isReadable(path)) {
            if (!allowSelectFiles() && Files.isRegularFile(path)) {
                return false;
            }
            String name = path.getFileName() == null ? path.toString() : path.getFileName().toString();
            return !name.startsWith("$") && !name.startsWith(".");
        }
        return false;
    }

    /**
     * Write directory to UI
     */
    default boolean writeDirectory(Path dir) {
        return true;
    }

    /**
     * Visit directory and all children
     */
    default boolean visitDirectory(Path dir, BasicFileAttributes attrs) {
        if (Files.isReadable(dir)) {
            String name = dir.getFileName() == null ? dir.toString() : dir.getFileName().toString();
            return !name.startsWith("$") && !name.startsWith(".");
        }
        return false;
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
        return getFilePath(params == null || !params.has("param0") ? null : Paths.get(params.getString("param0")));
    }

    default List<OptionModel> getFilePath(Path rootPath) {
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
                            if (SettingPluginOptionsFileExplorer.this.writeFile(path, attrs)) {
                                handlePath(path);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                            if (visitDirectory(dir, attrs)) {
                                if (writeDirectory(dir)) {
                                    handlePath(dir);
                                } else if (dir.equals(root) && skipRootInTreeStructure()) { // handleDir anyway if it root
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
                            OptionModel model = OptionModel.of(buildKey(path, root), buildTitle(path));
                            boolean isDirectory = Files.isDirectory(path);
                            // 1 - file, 2 - directory, 3 - empty directory
                            if (isDirectory) {
                                if (Files.list(path).findAny().isPresent()) {
                                    model.getJson().put("type", 2);
                                    model.setIcon("fas fa-folder");
                                } else {
                                    model.getJson().put("type", 3);
                                    model.setIcon("fas fa-folder-minus");
                                }
                            } else {
                                model.setIcon("fas fa-file-alt");
                                model.getJson().put("type", 1);
                            }
                            return model;
                        }
                    });
            List<OptionModel> result = new ArrayList<>(flatStructure() ? fs.values() : fs.containsKey(root) ?
                    fs.get(root).getOrCreateChildren() : Collections.emptyList());
            if (lazyLoading()) {
                handleRequestNextPath(result);
            }
            result.sort(pathComparator());
            return result;
        } catch (Exception ex) {
            throw new RuntimeException("Unable to fetch " + TouchHomeUtils.getErrorMessage(ex));
        }
    }

    default String buildKey(Path path, Path root) {
        String key = path.toString();
        if (skipRootInTreeStructure() && path.equals(root)) {
            key = null;
        }
        return key;
    }

    default String buildTitle(Path path) {
        return path.toString();
    }

    default void handleRequestNextPath(Collection<OptionModel> result) {
        for (OptionModel model : result) {
            if (model.getChildren() == null) {
                if (model.getJson().has("type") && model.getJson().getInt("type") == 2) {
                    model.getJson().put("requestNext", true);
                }
            } else {
                handleRequestNextPath(model.getChildren());
            }
        }
    }

    /**
     * Does allow select directories on UI.
     */
    default boolean allowSelectDirs() {
        return true;
    }

    /**
     * Does allow select files on UI.
     */
    default boolean allowSelectFiles() {
        return true;
    }

    default boolean skipRootInTreeStructure() {
        return true;
    }

    /**
     * Show all entries as a list or tree
     */
    default boolean flatStructure() {
        return false;
    }

    /**
     * Lazy loading next level
     */
    default boolean lazyLoading() {
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
        return value == null ? null : Paths.get(value);
    }
}
