package org.touchhome.bundle.api.setting;

import com.pivovarit.function.ThrowingBiPredicate;
import com.pivovarit.function.ThrowingPredicate;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.field.UIFieldType;
import org.touchhome.common.util.CommonUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

public interface SettingPluginOptionsFileExplorer extends SettingPluginOptionsRemovable<Path> {

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.TextSelectBoxDynamic;
    }

    default boolean allowUserInput() {
        return false;
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
     * Write directory to UI
     */
    default boolean writeDirectory(Path dir) {
        return true;
    }

    /**
     * Visit directory and all children
     */
    default boolean visitDirectory(Path dir, BasicFileAttributes attrs) {
        return visitDirectoryDefault(dir, attrs);
    }

    static boolean visitDirectoryDefault(Path dir, BasicFileAttributes attrs) {
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
        return getFilePath(rootPath == null ? rootPath() : rootPath, levels(),
                flatStructure(), lazyLoading(), skipRootInTreeStructure(), pathComparator(),
                this::visitDirectory, this::writeDirectory, this::writeFile, this::buildKey, this::buildTitle);
    }

    static List<OptionModel> getFilePath(Path root, int levels,
                                         boolean flatStructure,
                                         boolean lazyLoading,
                                         boolean skipRootInTreeStructure,
                                         @Nullable Comparator<OptionModel> pathComparator,
                                         @Nullable BiPredicate<Path, BasicFileAttributes> visitDirectory,
                                         @Nullable ThrowingPredicate<Path, Exception> writeDirectory,
                                         @Nullable ThrowingBiPredicate<Path, BasicFileAttributes, Exception> writeFile,
                                         @Nullable BiFunction<Path, Path, String> buildKey,
                                         @Nullable Function<Path, String> buildTitle) {
        try {
            if (root == null) {
                return Collections.emptyList();
            }
            BiPredicate<Path, BasicFileAttributes> visitDirectoryTest = visitDirectory == null ?
                    SettingPluginOptionsFileExplorer::visitDirectoryDefault : visitDirectory;
            ThrowingPredicate<Path, Exception> writeDirectoryTest = writeDirectory == null ? path -> true : writeDirectory;

            Map<Path, OptionModel> fs = new HashMap<>();
            Files.walkFileTree(root,
                    new HashSet<>(Collections.singletonList(FileVisitOption.FOLLOW_LINKS)),
                    Math.max(levels, 1), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                            try {
                                if (writeFile == null || writeFile.test(path, attrs)) {
                                    handlePath(path);
                                }
                            } catch (Exception ignore) {
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                            if (visitDirectoryTest.test(dir, attrs)) {
                                try {
                                    if (writeDirectoryTest.test(dir)) {
                                        handlePath(dir);
                                    } else if (dir.equals(root) && skipRootInTreeStructure) { // handleDir anyway if it root
                                        handlePath(dir);
                                    }
                                } catch (Exception ignore) {
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
                            if (model != null) {
                                if (!flatStructure && parent != null && fs.containsKey(parent)) {
                                    fs.get(parent).addChild(model);
                                }
                                fs.put(path, model);
                            }
                        }

                        @SneakyThrows
                        private OptionModel createOptionModelFromPath(Path path) {
                            String key = defaultIfEmpty(buildKey == null ? null : buildKey.apply(path, root),
                                    buildKeyDefault(skipRootInTreeStructure, path, root));
                            if (key == null) {
                                return null;
                            }
                            String title = defaultIfEmpty(buildTitle == null ? null : buildTitle.apply(path),
                                    path.toString());
                            OptionModel model = OptionModel.of(key, title);
                            boolean isDirectory = Files.isDirectory(path);
                            // 1 - file, 2 - directory, 3 - empty directory
                            model.put("translate", false);
                            if (isDirectory) {
                                if (Files.list(path).findAny().isPresent()) {
                                    model.put("type", 2);
                                    model.setIcon("fas fa-folder");
                                    model.setColor("#bdc500");
                                } else {
                                    model.put("type", 3);
                                    model.setIcon("fas fa-folder-minus");
                                    model.setColor("#95B8EC");
                                }
                            } else {
                                model.setIcon("fas fa-file-alt");
                                model.put("type", 1);
                            }
                            return model;
                        }
                    });
            List<OptionModel> result = new ArrayList<>(flatStructure ? fs.values() : fs.containsKey(root) ?
                    fs.get(root).getOrCreateChildren() : Collections.emptyList());
            if (lazyLoading) {
                SettingPluginOptionsFileExplorer.handleRequestNextPath(result);
            }
            result.sort(pathComparator == null ? Comparator.comparing(OptionModel::getTitleOrKey) : pathComparator);
            return result;
        } catch (Exception ex) {
            throw new RuntimeException("Unable to fetch " + CommonUtils.getErrorMessage(ex));
        }
    }

    static void handleRequestNextPath(Collection<OptionModel> result) {
        for (OptionModel model : result) {
            if (model.getChildren() == null) {
                if (model.has("type") && model.getJson().getInt("type") == 2) {
                    model.put("requestNext", true);
                }
            } else {
                SettingPluginOptionsFileExplorer.handleRequestNextPath(model.getChildren());
            }
        }
    }

    default boolean writeFile(Path path, BasicFileAttributes attrs) {
        return SettingPluginOptionsFileExplorer.writeFile(path, attrs, allowSelectFiles());
    }

    /**
     * Write file to UI
     */
    static boolean writeFile(Path path, BasicFileAttributes attrs, boolean allowSelectFiles) {
        if (Files.exists(path) && Files.isReadable(path)) {
            if (!allowSelectFiles && Files.isRegularFile(path)) {
                return false;
            }
            String name = path.getFileName() == null ? path.toString() : path.getFileName().toString();
            return !name.startsWith("$") && !name.startsWith(".");
        }
        return false;
    }

    default String buildKey(Path path, Path root) {
        return null;
    }

    static String buildKeyDefault(boolean skipRootInTreeStructure, Path path, Path root) {
        String key = path.toString();
        if (skipRootInTreeStructure && path.equals(root)) {
            key = null;
        }
        return key;
    }

    default String buildTitle(Path path) {
        return path.toString();
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
