package org.touchhome.bundle.api.fs.archive;

import com.pivovarit.function.ThrowingBiConsumer;
import com.pivovarit.function.ThrowingPredicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.fs.TreeNode;
import org.touchhome.bundle.api.ui.field.ProgressBar;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;

import static org.touchhome.bundle.api.fs.archive.ApacheCompress.archive;
import static org.touchhome.bundle.api.fs.archive.ApacheCompress.createSeven7InputStream;

@Log4j2
public final class ArchiveUtil {

    @SneakyThrows
    public static List<Path> unzip(@NotNull Path archive, @NotNull Path destination, @Nullable String password,
                                   boolean createArchiveNameDirectory,
                                   @Nullable ProgressBar progressBar, @NotNull UnzipFileIssueHandler handler) {
        if (progressBar != null) {
            progressBar.progress(0, "Unzip files. Calculate size...");
        }
        char[] pwd = Optional.ofNullable(StringUtils.trimToNull(password)).map(String::toCharArray).orElse(null);
        String fileName = archive.getFileName().toString();
        if (createArchiveNameDirectory) {
            destination = destination.resolve(FilenameUtils.removeExtension(fileName));
        }
        Files.createDirectories(destination);
        ArchiveFormat archiveFormat = ArchiveFormat.getHandlerByPath(archive);
        double fileSize = progressBar == null ? 1D : archiveFormat.size(archive, pwd);
        List<Path> paths = archiveFormat.unzip(archive, destination, pwd, progressBar, handler, fileSize);

        if (progressBar != null) {
            progressBar.progress(99, "Unzip files done.");
        }
        return paths;
    }

    @SneakyThrows
    public static List<Path> unzip(@NotNull Path path, @NotNull ArchiveUtil.ArchiveFormat archiveFormat,
                                   @NotNull Path destination,
                                   @Nullable String password, @Nullable ProgressBar progressBar,
                                   @NotNull UnzipFileIssueHandler handler) {
        if (progressBar != null) {
            progressBar.progress(0, "Unzip files. Calculate size...");
        }
        char[] pwd = Optional.ofNullable(StringUtils.trimToNull(password)).map(String::toCharArray).orElse(null);
        Files.createDirectories(destination);
        double fileSize = progressBar == null ? 1D : archiveFormat.size(path, pwd);
        List<Path> paths = archiveFormat.unzip(path, destination, pwd, progressBar, handler, fileSize);

        if (progressBar != null) {
            progressBar.progress(99, "Unzip files done.");
        }
        return paths;
    }

    public static void unzip(@NotNull Path file, @NotNull Path destination, @NotNull UnzipFileIssueHandler handler) {
        unzip(file, destination, null, true, null, handler);
    }

    public static boolean isArchive(@NotNull Path archive) {
        try {
            ArchiveFormat.getHandlerByPath(archive);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static void renameEntry(@NotNull Path archive, @NotNull String entryName, @NotNull String newName) {
        ArchiveFormat.getHandlerByPath(archive).renameEntry(archive, fixPath(entryName), newName);
    }

    @SneakyThrows
    public static List<File> getArchiveEntries(@NotNull Path archive, @Nullable String password) {
        return ArchiveFormat.getHandlerByPath(archive)
                .getArchiveEntries(archive, password == null ? null : password.toCharArray());
    }

    @SneakyThrows
    public static List<File> getChildren(@NotNull Path archive, @Nullable String entryId) {
        List<File> archiveEntries = getArchiveEntries(archive, null);
        return archiveEntries.stream().filter(f -> entryId.equals(f.getParent())).collect(Collectors.toList());
    }

    public static boolean isValidArchive(@NotNull Path archive) {
        try {
            if (!Files.isRegularFile(archive) || !Files.isReadable(archive)) {
                return false;
            }
            ArchiveFormat archiveFormat = ArchiveFormat.getHandlerByPath(archive);
            return archiveFormat.validateHandler.test(archive);
        } catch (Exception ex) {
            return false;
        }
    }

    @SneakyThrows
    public static InputStream downloadArchiveEntry(@NotNull Path archive, @NotNull String entryNames, @Nullable String password) {
        return ArchiveFormat.getHandlerByPath(archive).downloadArchiveEntry(archive, entryNames.replaceAll("\\\\", "/"),
                password == null ? null : password.toCharArray());
    }

    @SneakyThrows
    public static Set<Path> removeEntries(@NotNull Path archive, @NotNull Set<String> entryNames, @Nullable String password) {
        return ArchiveFormat.getHandlerByPath(archive)
                .removeEntries(archive, entryNames, password == null ? null : password.toCharArray());
    }

    public static void addToArchive(@NotNull Path archive, @NotNull Collection<TreeNode> files) {
        ArchiveFormat.getHandlerByPath(archive).addEntries(archive, files);
    }

    @SneakyThrows
    public static @NotNull Path zip(@NotNull List<Path> sources, @NotNull Path destination, ArchiveFormat archiveFormat,
                                    @Nullable String level, @Nullable String password, @Nullable ProgressBar progressBar) {
        if (progressBar != null) {
            progressBar.progress(0, "Zip files. Calculate size...");
        }
        if (!destination.getFileName().toString().endsWith(archiveFormat.name)) {
            destination = destination.resolveSibling(destination.getFileName() + "." + archiveFormat.name);
        }
        char[] pwd = Optional.ofNullable(StringUtils.trimToNull(password)).map(String::toCharArray).orElse(null);
        archiveFormat.zip(sources, destination, level, pwd, progressBar);

        if (progressBar != null) {
            progressBar.progress(99, "Zip files done.");
        }
        return destination;
    }

    private static String fixPath(String path) {
        return path.replaceAll("\\\\", "/");
    }

    private static Set<String> fixPath(Collection<String> pathList) {
        return pathList.stream().map(ArchiveUtil::fixPath).collect(Collectors.toSet());
    }

    private static void writeSeven7ArchiveEntry(boolean isDirectory, TreeNode treeNode, SevenZOutputFile sevenZOutput)
            throws IOException {
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setDirectory(isDirectory);
        entry.setName(treeNode.getName());
        Long lastUpdated = treeNode.getAttributes().getLastUpdated();
        entry.setLastModifiedDate(new Date(lastUpdated == null ? System.currentTimeMillis() : lastUpdated));
        sevenZOutput.putArchiveEntry(entry);
        sevenZOutput.write(treeNode.getInputStream());
        sevenZOutput.closeArchiveEntry();
    }

    private void downloadArchiveEntries(Path archive, Path targetPath, Set<String> entries) {
        ArchiveFormat.getHandlerByPath(archive).downloadArchiveEntries(archive, targetPath, fixPath(entries));
    }

    public enum UnzipFileIssueHandler {
        skip, replace, replaceNotMatch, error
    }

    @Getter
    @RequiredArgsConstructor
    public enum ArchiveFormat {
        tar("tar", true, path -> true, (archive, password) -> {
            return new TarArchiveInputStream(Files.newInputStream(archive));
        }, (file, level, password) -> {
            return new TarArchiveOutputStream(Files.newOutputStream(file));
        }),
        // tar bz2
        tarGZ2("tar.gz2", true, path -> true, (archive, password) -> {
            return new TarArchiveInputStream(new BZip2CompressorInputStream(Files.newInputStream(archive)));
        }, (file, level, password) -> {
            return new TarArchiveOutputStream(new BZip2CompressorOutputStream(Files.newOutputStream(file)));
        }),
        // tar bz2
        tarXZ("tar.xz", true, path -> true, (archive, password) -> {
            return new TarArchiveInputStream(new XZCompressorInputStream(Files.newInputStream(archive)));
        }, (file, level, password) -> {
            return new TarArchiveOutputStream(new XZCompressorOutputStream(Files.newOutputStream(file)));
        }),
        // tar gz
        tarGZ("tar.gz", true, path -> true, (archive, password) -> {
            return new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(archive)));
        }, (file, level, password) -> {
            return new TarArchiveOutputStream(new GzipCompressorOutputStream(Files.newOutputStream(file)));
        }),
        // jar
        jar("jar", true, path -> {
            new ZipFile(path.toFile()).close();
            return true;
        }, (archive, password) -> {
            return new JarArchiveInputStream(Files.newInputStream(archive));
        }, (file, level, password) -> {
            JarArchiveOutputStream out = new JarArchiveOutputStream(new BufferedOutputStream(Files.newOutputStream(file)));
            out.setLevel("low".equals(level) ? Deflater.BEST_SPEED :
                    "high".equals(level) ? Deflater.BEST_COMPRESSION : Deflater.DEFAULT_COMPRESSION);
            return out;
        }),
        // war
        war("war", true, path -> {
            new ZipFile(path.toFile()).close();
            return true;
        }, (archive, password) -> {
            return new JarArchiveInputStream(Files.newInputStream(archive));
        }, (file, level, password) -> {
            JarArchiveOutputStream out = new JarArchiveOutputStream(new BufferedOutputStream(Files.newOutputStream(file)));
            out.setLevel("low".equals(level) ? Deflater.BEST_SPEED :
                    "high".equals(level) ? Deflater.BEST_COMPRESSION : Deflater.DEFAULT_COMPRESSION);
            return out;
        }),
        // zip
        zip("zip", true, path -> {
            new ZipFile(path.toFile()).close();
            return true;
        }, (archive, password) -> {
            return new ZipArchiveInputStream(Files.newInputStream(archive));
        }, (file, level, password) -> {
            ZipArchiveOutputStream out = new ZipArchiveOutputStream(new BufferedOutputStream(Files.newOutputStream(file)));
            out.setLevel("low".equals(level) ? Deflater.BEST_SPEED :
                    "high".equals(level) ? Deflater.BEST_COMPRESSION : Deflater.DEFAULT_COMPRESSION);
            return out;
        }),
        // 7z
        sevenZ("7z", false, path -> {
            new SevenZFile(path.toFile()).close();
            return true;
        }, (path, password) -> {
            return createSeven7InputStream(path, password);
        }, (file, level, password) -> {
            return new ArchiveOutputStream() {
                final SevenZOutputFile target = new SevenZOutputFile(file.toFile());

                @Override
                public void putArchiveEntry(ArchiveEntry entry) throws IOException {
                    target.putArchiveEntry(entry);
                }

                @Override
                public void write(int b) throws IOException {
                    target.write(b);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    target.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    target.write(b, off, len);
                }

                @Override
                public void closeArchiveEntry() throws IOException {
                    target.closeArchiveEntry();
                }

                @Override
                public void finish() throws IOException {
                    target.finish();
                }

                @Override
                public void close() throws IOException {
                    target.close();
                }

                @Override
                public ArchiveEntry createArchiveEntry(File inputFile, String entryNames) throws IOException {
                    return target.createArchiveEntry(inputFile, entryNames);
                }
            };
        });

        private final String name;
        private final boolean hasBuildInFileSystem;
        private final ThrowingPredicate<Path, Exception> validateHandler;
        private final CreateInputStreamProducer createInputStreamProducer;
        private final CreateOutputStreamProducer createOutputStreamProducer;

        public static ArchiveFormat getHandlerByPath(Path path) {
            String filePath = path.toString();
            for (ArchiveFormat archiveFormat : ArchiveFormat.values()) {
                if (filePath.endsWith(archiveFormat.name)) {
                    return archiveFormat;
                }
            }
            throw new RuntimeException("Path " + path + " is not a archive");
        }

        public static ArchiveFormat getHandlerByExtension(String ext) {
            for (ArchiveFormat archiveFormat : ArchiveFormat.values()) {
                if (archiveFormat.name.equals(ext)) {
                    return archiveFormat;
                }
            }
            throw new RuntimeException("Unable to find unzip handle for file extension: " + ext);
        }

        public long size(@NotNull Path archive, char[] password) throws Exception {
            return size(createInputStreamProducer.createStream(archive, password));
        }

        public void zip(@NotNull Path source, @NotNull Path destination, @Nullable String level, char[] password,
                        @Nullable ProgressBar progressBar) throws Exception {
            List<Path> sources =
                    Stream.of(Objects.requireNonNull(source.toFile().listFiles())).map(File::toPath).collect(Collectors.toList());
            archive(sources, createOutputStreamProducer.createStream(destination, level, password), progressBar);
        }

        public void zip(@NotNull List<Path> sources, @NotNull Path destination, @Nullable String level, char[] password,
                        @Nullable ProgressBar progressBar) throws Exception {
            archive(sources, createOutputStreamProducer.createStream(destination, level, password), progressBar);
        }

        public InputStream downloadArchiveEntry(@NotNull Path archive, @NotNull String entryNames, char[] password)
                throws Exception {
            return ApacheCompress.downloadEntry(createInputStreamProducer.createStream(archive, password),
                    entryNames);
        }

        public List<File> getArchiveEntries(@NotNull Path archive, char[] password) throws Exception {
            return ApacheCompress.getArchiveEntries(createInputStreamProducer.createStream(archive, password));
        }

        public List<Path> unzip(@NotNull Path archive, @NotNull Path destination, char[] password,
                                @Nullable ProgressBar progressBar, @NotNull UnzipFileIssueHandler handler, double fileSize)
                throws Exception {
            return ApacheCompress.unzipCompress(createInputStreamProducer.createStream(archive, password), destination, handler,
                    fileSize, progressBar);
        }

        @SneakyThrows
        public void renameEntry(Path archive, String entryName, String newName) {
            if (hasBuildInFileSystem) {
                // may rename only files, not folders
                try (FileSystem archiveFS = FileSystems.newFileSystem(archive, ArchiveUtil.class.getClassLoader())) {
                    Path fsPath = archiveFS.getPath(entryName);
                    if (Files.isRegularFile(fsPath)) { // only regular path may be removed inside fs
                        Files.move(fsPath, fsPath.resolveSibling(newName), StandardCopyOption.REPLACE_EXISTING);
                        return;
                    }
                }
            }

            modifyArchive(archive, (tmpPath, list) -> {
                Path entryPath = tmpPath.resolve(entryName);
                Path newPath = entryPath.resolveSibling(newName);
                Files.move(entryPath, newPath);
            });
        }

        @SneakyThrows
        private void modifyArchive(Path archive, ThrowingBiConsumer<Path, List<Path>, Exception> consumer) {
            Path tmpPath = TouchHomeUtils.getTmpPath().resolve("tmp_archive_" + System.currentTimeMillis());
            Files.createDirectories(tmpPath);
            try {
                List<Path> list = unzip(archive, tmpPath, null, null, UnzipFileIssueHandler.replace, 0);
                consumer.accept(tmpPath, list);
                Files.delete(archive);
                zip(tmpPath, archive, null, null, null);
            } finally {
                FileUtils.deleteDirectory(tmpPath.toFile());
            }
        }

        @SneakyThrows
        public void addEntries(@NotNull Path archive, @NotNull Collection<TreeNode> files) {
            if (hasBuildInFileSystem) {
                try (FileSystem archiveFS = FileSystems.newFileSystem(archive, ArchiveUtil.class.getClassLoader())) {
                    TouchHomeUtils.addFiles(Paths.get(""), files, (path, treeNode) ->
                            archiveFS.getPath(path.toString() + treeNode.getName()));
                }
                return;
            } else if (this == sevenZ) {
                SevenZOutputFile sevenZOutput = new SevenZOutputFile(archive.toFile());
                TouchHomeUtils.addFiles(Paths.get(""), files, (path, treeNode) -> path.resolve(treeNode.getName()),
                        (treeNode, path) -> writeSeven7ArchiveEntry(false, treeNode, sevenZOutput),
                        (treeNode, path) -> writeSeven7ArchiveEntry(true, treeNode, sevenZOutput));
                sevenZOutput.close();
                return;
            }
            modifyArchive(archive,
                    (tmpPath, list) -> TouchHomeUtils.addFiles(tmpPath, files,
                            (path, treeNode) -> path.resolve(treeNode.getName())));
        }

        @SneakyThrows
        public void downloadArchiveEntries(Path archive, Path targetFolder, Set<String> entries) {
            ApacheCompress.downloadEntries(createInputStreamProducer.createStream(archive, null),
                    targetFolder, entries);
        }

        public Set<Path> removeEntries(@NotNull Path archive, @NotNull Set<String> entryNames, char[] password) throws Exception {
            entryNames = fixPath(entryNames);
            Set<Path> removedItems = new HashSet<>();
            if (hasBuildInFileSystem) {
                try (FileSystem zipFS = FileSystems.newFileSystem(archive, ArchiveUtil.class.getClassLoader())) {
                    for (String entryName : entryNames) {
                        removedItems.addAll(TouchHomeUtils.removeFileOrDirectory(zipFS.getPath(entryName)));
                    }
                }
                return removedItems;
            }
            Set<String> entryNamesToRemove = entryNames;
            modifyArchive(archive, (path, list) -> {
                for (Path item : list) {
                    String pathName = path.relativize(item).toString().replaceAll("\\\\", "/");
                    if (entryNamesToRemove.contains(pathName)) {
                        removedItems.addAll(TouchHomeUtils.removeFileOrDirectory(item));
                    }
                }
            });
            return removedItems;
        }

        @Override
        public String toString() {
            return name;
        }

        private long size(ArchiveInputStream stream) throws Exception {
            long fullSize = 0;
            ArchiveEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                if (!stream.canReadEntryData(entry)) {
                    continue;
                }
                long size = entry.getSize();
                fullSize += size > 0 ? size : 0;
            }
            stream.close();
            return fullSize;
        }
    }

    interface CreateInputStreamProducer {
        ArchiveInputStream createStream(@NotNull Path path, char[] password) throws Exception;
    }

    interface CreateOutputStreamProducer {
        ArchiveOutputStream createStream(@NotNull Path file, @Nullable String level, char[] password) throws Exception;
    }
}