package org.homio.api.fs.archive;

import static org.apache.commons.compress.archivers.examples.Archiver.EMPTY_FileVisitOption;
import static org.apache.commons.compress.utils.IOUtils.EMPTY_LINK_OPTIONS;
import static org.apache.commons.io.FileUtils.ONE_MB_BI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.IOUtils;
import org.homio.api.ui.field.ProgressBar;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ApacheCompress {

    public static void archive(List<Path> sources, ArchiveOutputStream out, ProgressBar progressBar) throws IOException {
        for (Path source : sources) {
            if (Files.isDirectory(source)) {
                Files.walkFileTree(source, EMPTY_FileVisitOption, Integer.MAX_VALUE,
                        new ArchiverFileVisitor(out, source.getParent()));
            } else {
                writeZipEntry(source, true, source.getParent(), out);
            }
        }
        out.finish();
        out.close();
    }

    private static void writeZipEntry(Path path, boolean isFile, Path directory, ArchiveOutputStream target) throws IOException {
        String name = directory.relativize(path).toString().replace('\\', '/');
        if (!name.isEmpty()) {
            ArchiveEntry archiveEntry =
                    target.createArchiveEntry(path, isFile || name.endsWith("/") ? name : name + "/", EMPTY_LINK_OPTIONS);
            target.putArchiveEntry(archiveEntry);
            if (isFile) {
                Files.copy(path, target);
            }
            target.closeArchiveEntry();
        }
    }

    public static List<Path> unzipSeven7Archive(Path path, Path destination, char[] password,
                                                ProgressBar progressBar,
                                                ArchiveUtil.UnzipFileIssueHandler handler, double fileSize) throws IOException {
        ArchiveInputStream stream = createSeven7InputStream(path, password);
        return ApacheCompress.unzipCompress(stream, destination, handler, fileSize, progressBar);
    }

    public static ArchiveInputStream createSeven7InputStream(Path path, char[] password) throws IOException {
        SevenZFile sevenZFile = new SevenZFile(path.toFile(), password);
        return new ArchiveInputStream() {

            @Override
            public ArchiveEntry getNextEntry() throws IOException {
                return sevenZFile.getNextEntry();
            }

            @Override
            public int read(byte[] buf, int offset, int numToRead) throws IOException {
                return sevenZFile.read(buf, offset, numToRead);
            }

            @Override
            public void close() throws IOException {
                sevenZFile.close();
            }
        };
    }

    @SneakyThrows
    public static List<Path> unzipCompress(@NotNull ArchiveInputStream stream, @NotNull Path destination,
                                           @NotNull ArchiveUtil.UnzipFileIssueHandler fileResolveHandler,
                                           double fileSize, @Nullable ProgressBar progressBar) {
        List<Path> paths = new ArrayList<>();
        int maxMb = (int) (fileSize / ONE_MB_BI.intValue());
        byte[] oneMBBuff = new byte[ONE_MB_BI.intValue()];
        OpenOption[] openOptions = fileResolveHandler == ArchiveUtil.UnzipFileIssueHandler.replace ?
                new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING} :
                new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE};
        ArchiveEntry entry;
        int nextStep = 1;
        int readBytes = 0;
        while ((entry = stream.getNextEntry()) != null) {
            if (!stream.canReadEntryData(entry)) {
                continue;
            }
            Path entryPath = destination.resolve(entry.getName());
            if (entry.isDirectory()) {
                if (!Files.isDirectory(entryPath)) {
                    paths.add(Files.createDirectories(entryPath));
                }
            } else {
                Path parent = entryPath.getParent();
                if (!Files.isDirectory(parent)) {
                    paths.add(Files.createDirectories(parent));
                }

                if (Files.exists(entryPath)) {
                    switch (fileResolveHandler) {
                        case skip:
                            continue;
                        case replace: // already in OpenOptions
                            break;
                        case replaceNotMatch:
                            Path tmpPath = CommonUtils.getTmpPath().resolve(entry.getName());
                            Files.copy(stream, tmpPath, StandardCopyOption.REPLACE_EXISTING);
                            if (IOUtils.contentEquals(Files.newInputStream(tmpPath), Files.newInputStream(entryPath))) {
                                Files.delete(tmpPath);
                                continue;
                            }
                            break;
                        case error:
                            throw new FileAlreadyExistsException("Unarchive file '" + entry + "' already exists");
                    }
                }

                paths.add(entryPath);

                try (OutputStream out = Files.newOutputStream(entryPath, openOptions)) {
                    int bytesToRead = (int) entry.getSize();
                    if (bytesToRead == -1) {
                        IOUtils.copy(stream, out);
                    }
                    while (bytesToRead > 0) {
                        long bytes = Math.min(bytesToRead, ONE_MB_BI.intValue());
                        byte[] content = bytes == ONE_MB_BI.intValue() ? oneMBBuff : new byte[(int) bytes];
                        stream.read(content);
                        out.write(content);
                        bytesToRead -= bytes;
                        readBytes += bytes;

                        if (readBytes / ONE_MB_BI.doubleValue() > nextStep) {
                            nextStep++;
                            if (progressBar != null) {
                                progressBar.progress((readBytes / fileSize * 100) * 0.99, // max 99%
                                        "Extract " + readBytes / ONE_MB_BI.intValue() + "Mb. of " + maxMb + " Mb.");
                            }
                        }
                    }
                }
            }
        }
        stream.close();
        return paths;
    }

    @SneakyThrows
    public static InputStream downloadEntry(ArchiveInputStream stream, String entryName) {
        ArchiveEntry entry;
        try {
            while ((entry = stream.getNextEntry()) != null) {
                if (!stream.canReadEntryData(entry)) {
                    continue;
                }
                if (entry.getName().equals(entryName)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    IOUtils.copy(stream, out);
                    return new ByteArrayInputStream(out.toByteArray());
                }
            }
        } finally {
            stream.close();
        }
        return null;
    }

    @SneakyThrows
    public static void downloadEntries(ArchiveInputStream stream, Path targetFolder, Set<String> entries) {
        ArchiveEntry entry;
        Files.createDirectories(targetFolder);
        while ((entry = stream.getNextEntry()) != null) {
            if (!stream.canReadEntryData(entry)) {
                continue;
            }
            ArchiveEntry finalEntry = entry;

            if (entries.stream().anyMatch(e -> finalEntry.getName().startsWith(e))) {
                Path targetPath = targetFolder.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(stream, targetPath);
                }
            }
        }
        stream.close();
    }

    @SneakyThrows
    public static List<File> getArchiveEntries(ArchiveInputStream stream) {
        ArchiveEntry entry;
        List<File> files = new ArrayList<>();
        while ((entry = stream.getNextEntry()) != null) {
            if (!stream.canReadEntryData(entry)) {
                continue;
            }

            ArchiveEntry archiveEntry = entry;
            files.add(new File(archiveEntry.getName()) {

                @Override
                public String[] list() {
                    return new String[]{""};
                }

                @Override
                public boolean isDirectory() {
                    return archiveEntry.isDirectory();
                }

                @Override
                public long length() {
                    return archiveEntry.getSize();
                }

                @Override
                public long lastModified() {
                    return archiveEntry.getLastModifiedDate().getTime();
                }
            });
        }
        stream.close();
        return files;
    }

    @AllArgsConstructor
    private static class ArchiverFileVisitor extends SimpleFileVisitor<Path> {

        private final ArchiveOutputStream target;
        private final Path directory;

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            return visit(dir, attrs, false);
        }

        protected FileVisitResult visit(Path path, BasicFileAttributes attrs, boolean isFile) throws IOException {
            writeZipEntry(path, isFile, directory, target);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            return visit(file, attrs, true);
        }
    }
}
