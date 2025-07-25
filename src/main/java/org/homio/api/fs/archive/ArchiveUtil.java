package org.homio.api.fs.archive;

import static java.lang.String.format;
import static org.homio.api.fs.archive.ArchiveUtil.UnzipFileIssueHandler.replace;

import com.pivovarit.function.ThrowingBiConsumer;
import com.pivovarit.function.ThrowingPredicate;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
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
import org.homio.api.fs.TreeNode;
import org.homio.api.util.CommonUtils;
import org.homio.hquery.Curl;
import org.homio.hquery.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@Log4j2
public final class ArchiveUtil {

  public static void unzipAndMove(
      @NotNull ProgressBar progressBar, @NotNull Path archive, @NotNull Path targetPath)
      throws IOException {
    progressBar.progress(70, format("Unzip %s sources", archive));
    Path workingPath = archive.getParent();
    List<Path> files = ArchiveUtil.unzip(archive, workingPath, null, false, progressBar, replace);
    if (!files.isEmpty()) {
      /*Path unzipFolder = CommonUtils.getTmpPath().relativize(files.iterator().next());
      while (unzipFolder.getParent() != null) {
          unzipFolder = unzipFolder.getParent();
      }*/
      Files.delete(archive);
      CommonUtils.deletePath(targetPath);
      File srcDir = workingPath.toFile();
      // we need copy only desired files if we unzip many files to single folder
      if (files.size() > 1) {
        File[] listFiles = Objects.requireNonNull(srcDir.listFiles());
        if (listFiles.length == 1) {
          srcDir = listFiles[0];
        }
      }
      // Path unzipPath = CommonUtils.getTmpPath().resolve(unzipFolder);
      FileUtils.copyDirectory(srcDir, targetPath.toFile());
    }
    FileUtils.deleteDirectory(workingPath.toFile());
  }

  /**
   * Download archive file from url, extract, delete archive
   *
   * @param url - url of archived source
   * @param archiveFileName - archive file name
   * @param progressBar - progress bar to print progress
   * @return unarchived downloaded folder
   */
  @SneakyThrows
  public static @NotNull List<Path> downloadAndExtract(
      @NotNull String url, @NotNull String archiveFileName, @NotNull ProgressBar progressBar) {
    return downloadAndExtract(url, archiveFileName, progressBar, CommonUtils.getInstallPath());
  }

  @SneakyThrows
  public static @NotNull List<Path> downloadAndExtract(
      @NotNull String url,
      @NotNull String archiveFileName,
      @NotNull ProgressBar progressBar,
      @NotNull Path targetFolder) {
    progressBar.progress(0, format("Downloading '%s' from url '%s'", archiveFileName, url));
    Path archivePath = CommonUtils.getTmpPath().resolve(archiveFileName);
    Curl.downloadWithProgress(url, archivePath, progressBar);
    progressBar.progress(90, format("Extracting '%s' to path '%s'", archivePath, targetFolder));
    List<Path> files =
        ArchiveUtil.unzip(
            archivePath, targetFolder, null, false, progressBar, UnzipFileIssueHandler.replace);
    Files.delete(archivePath);
    return files;
  }

  /** Special case for unzip from internal jars */
  @SneakyThrows
  public static List<Path> unzip(
      @NotNull URL url,
      @NotNull String urlFileName,
      @NotNull Path destination,
      @Nullable String password,
      boolean createArchiveNameDirectory,
      @Nullable ProgressBar progressBar,
      @NotNull UnzipFileIssueHandler handler) {
    InputStream stream = CommonUtils.getClassLoader().getResourceAsStream(url.toString());
    FileSystem fileSystem = null;
    if (stream == null) {
      fileSystem = FileSystems.newFileSystem(url.toURI(), Collections.emptyMap());
      Path filesPath = fileSystem.getPath(urlFileName);
      stream = Files.exists(filesPath) ? Files.newInputStream(filesPath) : null;
    }
    if (stream != null) {
      String addonJar = url.getFile().replaceAll(".jar!/", "_");
      addonJar = addonJar.substring(addonJar.lastIndexOf("/") + 1);
      Path targetPath = destination.resolve(destination.resolve(addonJar));
      if (!Files.exists(targetPath) || Files.size(targetPath) != stream.available()) {
        // copy files
        log.info("Copy resource <{}>", url);
        Files.createDirectories(targetPath.getParent());
        Files.copy(stream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Unzip resource <{}>", targetPath);
        ArchiveUtil.unzip(
            targetPath,
            targetPath.getParent(),
            password,
            createArchiveNameDirectory,
            progressBar,
            handler);
        log.info("Done copy resource <{}>", url);
      }
      stream.close();
      if (fileSystem != null) {
        fileSystem.close();
      }
    }
    return null;
  }

  @SneakyThrows
  public static List<Path> unzip(
      @NotNull Path archive,
      @NotNull Path destination,
      @Nullable String password,
      boolean createArchiveNameDirectory,
      @Nullable ProgressBar progressBar,
      @NotNull UnzipFileIssueHandler handler) {
    if (progressBar != null) {
      progressBar.progress(0, "Unzip files. Calculate size...");
    }
    char[] pwd =
        Optional.ofNullable(StringUtils.trimToNull(password)).map(String::toCharArray).orElse(null);
    String fileName = archive.getFileName().toString();
    if (createArchiveNameDirectory) {
      destination = destination.resolve(FilenameUtils.removeExtension(fileName));
    }
    Files.createDirectories(destination);
    ArchiveFormat archiveFormat = ArchiveFormat.getHandlerByPath(archive);
    double fileSize = progressBar == null ? 1D : archiveFormat.size(archive, pwd);
    List<Path> paths =
        archiveFormat.unzip(archive, destination, pwd, progressBar, handler, fileSize);

    if (progressBar != null) {
      progressBar.progress(99, "Unzip files done.");
    }
    return paths;
  }

  @SneakyThrows
  public static List<Path> unzip(
      @NotNull Path path,
      @NotNull ArchiveUtil.ArchiveFormat archiveFormat,
      @NotNull Path destination,
      @Nullable String password,
      @Nullable ProgressBar progressBar,
      @NotNull UnzipFileIssueHandler handler) {
    if (progressBar != null) {
      progressBar.progress(0, "Unzip files. Calculate size...");
    }
    char[] pwd =
        Optional.ofNullable(StringUtils.trimToNull(password)).map(String::toCharArray).orElse(null);
    Files.createDirectories(destination);
    double fileSize = progressBar == null ? 1D : archiveFormat.size(path, pwd);
    List<Path> paths = archiveFormat.unzip(path, destination, pwd, progressBar, handler, fileSize);

    if (progressBar != null) {
      progressBar.progress(99, "Unzip files done.");
    }
    return paths;
  }

  public static List<Path> unzip(
      @NotNull Path file, @NotNull Path destination, @NotNull UnzipFileIssueHandler handler) {
    return unzip(file, destination, null, true, null, handler);
  }

  public static boolean isArchive(@NotNull Path archive) {
    try {
      ArchiveFormat.getHandlerByPath(archive);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  public static void renameEntry(
      @NotNull Path archive, @NotNull String entryName, @NotNull String newName) {
    ArchiveFormat.getHandlerByPath(archive).renameEntry(archive, fixPath(entryName), newName);
  }

  @SneakyThrows
  public static List<File> getArchiveEntries(@NotNull Path archive, @Nullable String password) {
    return ArchiveFormat.getHandlerByPath(archive)
        .getArchiveEntries(archive, password == null ? null : password.toCharArray());
  }

  @SneakyThrows
  public static List<File> getChildren(@NotNull Path archive, @NotNull String entryId) {
    List<File> archiveEntries = getArchiveEntries(archive, null);
    return archiveEntries.stream()
        .filter(f -> entryId.equals(f.getParent()))
        .collect(Collectors.toList());
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
  public static InputStream downloadArchiveEntry(
      @NotNull Path archive, @NotNull String entryNames, @Nullable String password) {
    return ArchiveFormat.getHandlerByPath(archive)
        .downloadArchiveEntry(
            archive,
            entryNames.replaceAll("\\\\", "/"),
            password == null ? null : password.toCharArray());
  }

  @SneakyThrows
  public static Set<Path> removeEntries(
      @NotNull Path archive, @NotNull Set<String> entryNames, @Nullable String password) {
    return ArchiveFormat.getHandlerByPath(archive)
        .removeEntries(archive, entryNames, password == null ? null : password.toCharArray());
  }

  public static void addToArchive(@NotNull Path archive, @NotNull Collection<TreeNode> files) {
    ArchiveFormat.getHandlerByPath(archive).addEntries(archive, files);
  }

  /**
   * Archive source directory to destination.
   *
   * @param sourceFolder - existed source dir
   * @param destinationFile - dest file
   * @param archiveFormat -format
   * @param progressBar - progress
   * @param includeParentFolder - does include parent folder to archive or not
   */
  @SneakyThrows
  public static void zip(
      @NotNull Path sourceFolder,
      @NotNull Path destinationFile,
      ArchiveFormat archiveFormat,
      @Nullable ProgressBar progressBar,
      boolean includeParentFolder) {
    if (!Files.isDirectory(sourceFolder)) {
      throw new IllegalArgumentException("SourceFolder must be a directory");
    }
    Set<Path> sources =
        includeParentFolder
            ? Set.of(sourceFolder)
            : Arrays.stream(Objects.requireNonNull(sourceFolder.toFile().listFiles()))
                .map(File::toPath)
                .collect(Collectors.toSet());
    zip(sources, destinationFile, archiveFormat, null, null, progressBar);
  }

  @SneakyThrows
  public static void zip(
      @NotNull Collection<Path> sources,
      @NotNull Path destination,
      ArchiveFormat archiveFormat,
      @Nullable String level,
      @Nullable String password,
      @Nullable ProgressBar progressBar) {
    if (progressBar != null) {
      progressBar.progress(0, "Zip files. Calculate size...");
    }
    if (!destination.getFileName().toString().endsWith(archiveFormat.name)) {
      destination =
          destination.resolveSibling(destination.getFileName() + "." + archiveFormat.name);
    }
    char[] pwd =
        Optional.ofNullable(StringUtils.trimToNull(password)).map(String::toCharArray).orElse(null);
    archiveFormat.zip(sources, destination, level, pwd, progressBar);

    if (progressBar != null) {
      progressBar.progress(99, "Zip files done.");
    }
  }

  /**
   * Copy file or directory
   *
   * @param archive - archive file
   * @param sourcePath - source in archive
   * @param targetPath - target on fs
   */
  @SneakyThrows
  public static void copyEntries(
      @NotNull Path archive,
      @NotNull Set<Path> sourcePath,
      @NotNull Path targetPath,
      boolean isSkipExisted) {
    ArchiveFormat archiveFormat = ArchiveFormat.getHandlerByPath(archive);
    Set<String> entries = sourcePath.stream().map(Path::toString).collect(Collectors.toSet());
    archiveFormat.downloadArchiveEntries(archive, targetPath, entries, isSkipExisted);
  }

  public static String fixPath(@NotNull String path) {
    return path.replaceAll("\\\\", "/");
  }

  public static Set<String> fixPath(@NotNull Collection<String> pathList) {
    return pathList.stream().map(ArchiveUtil::fixPath).collect(Collectors.toSet());
  }

  private static void writeSeven7ArchiveEntry(
      boolean isDirectory, @NotNull TreeNode treeNode, @NotNull SevenZOutputFile sevenZOutput)
      throws IOException {
    SevenZArchiveEntry entry = new SevenZArchiveEntry();
    entry.setDirectory(isDirectory);
    entry.setName(treeNode.getName());
    Long lastUpdated = treeNode.getAttributes().getLastUpdated();
    entry.setLastModifiedDate(
        new Date(lastUpdated == null ? System.currentTimeMillis() : lastUpdated));
    sevenZOutput.putArchiveEntry(entry);
    sevenZOutput.write(treeNode.getInputStream());
    sevenZOutput.closeArchiveEntry();
  }

  private void downloadArchiveEntries(
      @NotNull Path archive,
      @NotNull Path targetPath,
      @NotNull Set<String> entries,
      boolean isSkipExisted) {
    ArchiveFormat.getHandlerByPath(archive)
        .downloadArchiveEntries(archive, targetPath, entries, isSkipExisted);
  }

  public enum UnzipFileIssueHandler {
    skip,
    replace,
    replaceNotMatch,
    error
  }

  @Getter
  @RequiredArgsConstructor
  public enum ArchiveFormat {
    tar(
        "tar",
        true,
        path -> true,
        (archive, password) -> new TarArchiveInputStream(Files.newInputStream(archive)),
        (file, level, password) -> new TarArchiveOutputStream(Files.newOutputStream(file))),
    // tar bz2
    tarGZ2(
        "tar.gz2",
        true,
        path -> true,
        (archive, password) ->
            new TarArchiveInputStream(
                new BZip2CompressorInputStream(Files.newInputStream(archive))),
        (file, level, password) ->
            new TarArchiveOutputStream(
                new BZip2CompressorOutputStream(Files.newOutputStream(file)))),
    // tar bz2
    tarXZ(
        "tar.xz",
        true,
        path -> true,
        (archive, password) ->
            new TarArchiveInputStream(new XZCompressorInputStream(Files.newInputStream(archive))),
        (file, level, password) ->
            new TarArchiveOutputStream(new XZCompressorOutputStream(Files.newOutputStream(file)))),
    // tar gz
    tarGZ(
        "tar.gz",
        true,
        path -> true,
        (archive, password) ->
            new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(archive))),
        (file, level, password) ->
            new TarArchiveOutputStream(
                new GzipCompressorOutputStream(Files.newOutputStream(file)))),
    // jar
    jar(
        "jar",
        true,
        path -> {
          new ZipFile(path.toFile()).close();
          return true;
        },
        (archive, password) -> new JarArchiveInputStream(Files.newInputStream(archive)),
        (file, level, password) -> {
          JarArchiveOutputStream out =
              new JarArchiveOutputStream(new BufferedOutputStream(Files.newOutputStream(file)));
          out.setLevel(
              "low".equals(level)
                  ? Deflater.BEST_SPEED
                  : "high".equals(level)
                      ? Deflater.BEST_COMPRESSION
                      : Deflater.DEFAULT_COMPRESSION);
          return out;
        }),
    // war
    war(
        "war",
        true,
        path -> {
          new ZipFile(path.toFile()).close();
          return true;
        },
        (archive, password) -> new JarArchiveInputStream(Files.newInputStream(archive)),
        (file, level, password) -> {
          JarArchiveOutputStream out =
              new JarArchiveOutputStream(new BufferedOutputStream(Files.newOutputStream(file)));
          out.setLevel(
              "low".equals(level)
                  ? Deflater.BEST_SPEED
                  : "high".equals(level)
                      ? Deflater.BEST_COMPRESSION
                      : Deflater.DEFAULT_COMPRESSION);
          return out;
        }),
    // zip
    zip(
        "zip",
        true,
        path -> {
          new ZipFile(path.toFile()).close();
          return true;
        },
        (archive, password) -> new ZipArchiveInputStream(Files.newInputStream(archive)),
        (file, level, password) -> {
          ZipArchiveOutputStream out =
              new ZipArchiveOutputStream(new BufferedOutputStream(Files.newOutputStream(file)));
          out.setLevel(
              "low".equals(level)
                  ? Deflater.BEST_SPEED
                  : "high".equals(level)
                      ? Deflater.BEST_COMPRESSION
                      : Deflater.DEFAULT_COMPRESSION);
          return out;
        }),
    // 7z
    sevenZ(
        "7z",
        false,
        path -> {
          new SevenZFile(path.toFile()).close();
          return true;
        },
        ApacheCompress::createSeven7InputStream,
        (file, level, password) ->
            new ArchiveOutputStream() {
              final SevenZOutputFile target = new SevenZOutputFile(file.toFile());

              @Override
              public void putArchiveEntry(ArchiveEntry entry) {
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
              public ArchiveEntry createArchiveEntry(File inputFile, String entryNames) {
                return target.createArchiveEntry(inputFile, entryNames);
              }
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

    public void zip(
        @NotNull Path source,
        @NotNull Path destination,
        @Nullable String level,
        char[] password,
        @Nullable ProgressBar progressBar)
        throws Exception {
      List<Path> sources =
          Stream.of(Objects.requireNonNull(source.toFile().listFiles()))
              .map(File::toPath)
              .collect(Collectors.toList());
      ArchiveOutputStream stream =
          createOutputStreamProducer.createStream(destination, level, password);
      ApacheCompress.archive(sources, stream, progressBar);
    }

    public void zip(
        @NotNull Collection<Path> sources,
        @NotNull Path destination,
        @Nullable String level,
        char[] password,
        @Nullable ProgressBar progressBar)
        throws Exception {
      ApacheCompress.archive(
          sources,
          createOutputStreamProducer.createStream(destination, level, password),
          progressBar);
    }

    public InputStream downloadArchiveEntry(
        @NotNull Path archive, @NotNull String entryNames, char[] password) throws Exception {
      return ApacheCompress.downloadEntry(
          createInputStreamProducer.createStream(archive, password), entryNames);
    }

    public List<File> getArchiveEntries(@NotNull Path archive, char[] password) throws Exception {
      return ApacheCompress.getArchiveEntries(
          createInputStreamProducer.createStream(archive, password));
    }

    public List<Path> unzip(
        @NotNull Path archive,
        @NotNull Path destination,
        char[] password,
        @Nullable ProgressBar progressBar,
        @NotNull UnzipFileIssueHandler handler,
        double fileSize)
        throws Exception {
      return ApacheCompress.unzipCompress(
          createInputStreamProducer.createStream(archive, password),
          destination,
          handler,
          fileSize,
          progressBar);
    }

    @SneakyThrows
    public void renameEntry(Path archive, String entryName, String newName) {
      if (hasBuildInFileSystem) {
        // may rename only files, not folders
        try (FileSystem archiveFS =
            FileSystems.newFileSystem(archive, ArchiveUtil.class.getClassLoader())) {
          Path fsPath = archiveFS.getPath(entryName);
          if (Files.isRegularFile(fsPath)) { // only regular path may be removed inside fs
            Files.move(fsPath, fsPath.resolveSibling(newName), StandardCopyOption.REPLACE_EXISTING);
            return;
          }
        }
      }

      modifyArchive(
          archive,
          (tmpPath, list) -> {
            Path entryPath = tmpPath.resolve(entryName);
            Path newPath = entryPath.resolveSibling(newName);
            Files.move(entryPath, newPath);
          });
    }

    @SneakyThrows
    public void addEntries(@NotNull Path archive, @NotNull Collection<TreeNode> files) {
      if (hasBuildInFileSystem) {
        try (FileSystem archiveFS =
            FileSystems.newFileSystem(archive, ArchiveUtil.class.getClassLoader())) {
          CommonUtils.addFiles(
              Paths.get(""),
              files,
              (path, treeNode) -> archiveFS.getPath(path.toString() + treeNode.getName()));
        }
        return;
      } else if (this == sevenZ) {
        SevenZOutputFile sevenZOutput = new SevenZOutputFile(archive.toFile());
        CommonUtils.addFiles(
            Paths.get(""),
            files,
            (path, treeNode) -> path.resolve(treeNode.getName()),
            (treeNode, path) -> writeSeven7ArchiveEntry(false, treeNode, sevenZOutput),
            (treeNode, path) -> writeSeven7ArchiveEntry(true, treeNode, sevenZOutput));
        sevenZOutput.close();
        return;
      }
      modifyArchive(
          archive,
          (tmpPath, list) ->
              CommonUtils.addFiles(
                  tmpPath, files, (path, treeNode) -> path.resolve(treeNode.getName())));
    }

    @SneakyThrows
    public void downloadArchiveEntries(
        Path archive, Path targetFolder, Set<String> entries, boolean isSkipExisted) {
      ApacheCompress.downloadEntries(
          createInputStreamProducer.createStream(archive, null),
          targetFolder,
          entries,
          isSkipExisted);
    }

    public Set<Path> removeEntries(
        @NotNull Path archive, @NotNull Set<String> entryNames, char[] password) throws Exception {
      entryNames = fixPath(entryNames);
      Set<Path> removedItems = new HashSet<>();
      if (hasBuildInFileSystem) {
        try (FileSystem zipFS =
            FileSystems.newFileSystem(archive, ArchiveUtil.class.getClassLoader())) {
          for (String entryName : entryNames) {
            removedItems.addAll(CommonUtils.removeFileOrDirectory(zipFS.getPath(entryName)));
          }
        }
        return removedItems;
      }
      Set<String> entryNamesToRemove = entryNames;
      modifyArchive(
          archive,
          (path, list) -> {
            for (Path item : list) {
              String pathName = path.relativize(item).toString().replaceAll("\\\\", "/");
              if (entryNamesToRemove.contains(pathName)) {
                removedItems.addAll(CommonUtils.removeFileOrDirectory(item));
              }
            }
          });
      return removedItems;
    }

    @Override
    public String toString() {
      return name;
    }

    @SneakyThrows
    private void modifyArchive(
        Path archive, ThrowingBiConsumer<Path, List<Path>, Exception> consumer) {
      Path tmpPath = CommonUtils.getTmpPath().resolve("tmp_archive_" + System.currentTimeMillis());
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

    ArchiveOutputStream createStream(@NotNull Path file, @Nullable String level, char[] password)
        throws Exception;
  }
}
