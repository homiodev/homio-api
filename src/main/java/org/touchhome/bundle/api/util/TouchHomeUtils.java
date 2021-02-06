package org.touchhome.bundle.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.touchhome.bundle.api.model.ProgressBar;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.apache.commons.io.FileUtils.ONE_MB_BI;

@Log4j2
public class TouchHomeUtils {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Path TMP_FOLDER = Paths.get(FileUtils.getTempDirectoryPath());
    @Getter
    private static final Path filesPath;
    @Getter
    private static final Path installPath;
    @Getter
    private static final Path externalJarClassPath;
    @Getter
    private static final Path bundlePath;
    @Getter
    private static final Path mediaPath;
    @Getter
    private static final Path sshPath;
    public static OsName OS_NAME = detectOs();
    public static String MACHINE_IP_ADDRESS = "127.0.0.1";
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static Path rootPath;
    private static Map<String, ClassLoader> bundleClassLoaders = new HashMap<>();

    // map for store different statuses
    @Getter
    private static Map<String, AtomicInteger> statusMap = new ConcurrentHashMap<>();

    static {
        if (SystemUtils.IS_OS_WINDOWS) {
            rootPath = SystemUtils.getUserHome().toPath().resolve("touchhome");
        } else {
            rootPath = Paths.get("/opt/touchhome");
        }
        installPath = getOrCreatePath("installs");
        filesPath = getOrCreatePath("asm_files");
        externalJarClassPath = getOrCreatePath("external_jars");
        sshPath = getOrCreatePath("ssh");
        bundlePath = getOrCreatePath("bundles");
        mediaPath = getOrCreatePath("media");
    }

    public static JSONObject putOpt(JSONObject jsonObject, String key, Object value) {
        if (StringUtils.isNotEmpty(key) && value != null) {
            jsonObject.put(key, value);
        }
        return jsonObject;
    }

    public static void addClassLoader(String bundleName, ClassLoader classLoader) {
        bundleClassLoaders.put(bundleName, classLoader);
    }

    public static void removeClassLoader(String bundleName) {
        bundleClassLoaders.remove(bundleName);
    }

    @SneakyThrows
    public static <T> T readAndMergeJSON(String resource, T targetObject) {
        ObjectReader updater = OBJECT_MAPPER.readerForUpdating(targetObject);
        ArrayList<ClassLoader> classLoaders = new ArrayList<>(bundleClassLoaders.values());
        classLoaders.add(TouchHomeUtils.class.getClassLoader());

        for (ClassLoader classLoader : classLoaders) {
            for (URL url : Collections.list(classLoader.getResources(resource))) {
                updater.readValue(url);
            }
        }
        return targetObject;
    }

    @SneakyThrows
    public static <T> List<T> readJSON(String resource, Class<T> targetClass) {
        Enumeration<URL> resources = TouchHomeUtils.class.getClassLoader().getResources(resource);
        List<T> list = new ArrayList<>();
        while (resources.hasMoreElements()) {
            list.add(OBJECT_MAPPER.readValue(resources.nextElement(), targetClass));
        }
        return list;
    }

    public static ResponseEntity<InputStreamResource> inputStreamToResource(InputStream stream, MediaType contentType) {
        try {
            return ResponseEntity.ok()
                    .contentLength(stream.available())
                    .contentType(contentType)
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static String getTimestampString() {
        return getTimestampString(new Date());
    }

    private static String getTimestampString(Date date) {
        return dateFormat.format(date);
    }

    public static List<Date> range(Date minDate, Date maxDate) {
        long time = (maxDate.getTime() - minDate.getTime()) / 10;
        List<Date> dates = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            dates.add(new Date(minDate.getTime() + time * i));
        }
        return dates;
    }

    public static List<String> readFile(String fileName) {
        try {
            return IOUtils.readLines(TouchHomeUtils.class.getClassLoader().getResourceAsStream(fileName), Charset.defaultCharset());
        } catch (Exception ex) {
            log.error(TouchHomeUtils.getErrorMessage(ex), ex);

        }
        return Collections.emptyList();
    }

    @SneakyThrows
    public static String getResourceAsString(String bundle, String resource) {
        return IOUtils.toString(getResource(bundle, resource), Charset.defaultCharset());
    }

    @SneakyThrows
    public static URL getResource(String bundle, String resource) {
        if (bundle != null && bundleClassLoaders.containsKey(bundle)) {
            return bundleClassLoaders.get(bundle).getResource(resource);
        }
        URL resourceURL = null;
        ArrayList<URL> urls = Collections.list(TouchHomeUtils.class.getClassLoader().getResources(resource));
        if (urls.size() == 1) {
            resourceURL = urls.get(0);
        } else if (urls.size() > 1 && bundle != null) {
            resourceURL = urls.stream().filter(url -> url.getFile().contains(bundle)).findAny().orElse(null);
        }
        return resourceURL;
    }

    public static Path path(String path) {
        return rootPath.resolve(path);
    }

    public static String getErrorMessage(Throwable ex) {
        if (ex == null) {
            return null;
        }
        if (ex instanceof NullPointerException || ex.getCause() instanceof NullPointerException) {
            return ex.getStackTrace()[0].toString();
        }
        return ex.getCause() == null ? ex.toString() : ex.getCause().toString();
    }

    public static String toTmpFile(String uniqueID, String suffix, ByteArrayOutputStream outputStream) throws IOException {
        Path tempFile = Files.createTempFile(uniqueID, suffix);
        Files.write(tempFile, outputStream.toByteArray());
        return "rest/download/tmp/" + TMP_FOLDER.relativize(tempFile).toString();
    }

    public static Path fromTmpFile(String str) {
        Path path = TMP_FOLDER.resolve(str);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Unable to find file: " + str);
        }
        return path;
    }

    @SneakyThrows
    public static FileSystem getOrCreateNewFileSystem(String fileSystemPath) {
        if (fileSystemPath == null) {
            return FileSystems.getDefault();
        }
        try {
            return FileSystems.getFileSystem(URI.create(fileSystemPath));
        } catch (Exception ex) {
            return FileSystems.newFileSystem(URI.create(fileSystemPath), Collections.emptyMap());
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static Path resolvePath(String... path) {
        Path relativePath = Paths.get(rootPath.toString(), path);
        if (Files.notExists(relativePath)) {
            try {
                Files.createDirectories(relativePath);
            } catch (Exception ex) {
                log.error("Unable to create path: <{}>", relativePath);
                throw new RuntimeException("Unable to create path: " + relativePath);
            }
        }
        return relativePath;
    }

    @SneakyThrows
    private static Path createDirectoriesIfNotExists(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectory(path);
            } catch (Exception ex) {
                log.error("Unable to create path: <{}>", path.toAbsolutePath().toString());
            }
        }
        return path;
    }

    @SneakyThrows
    public static Map<String, String> readPropertiesMerge(String path) {
        Map<String, String> map = new HashMap<>();
        readProperties(path).forEach(map::putAll);
        return map;
    }

    @SneakyThrows
    private static List<Map<String, String>> readProperties(String path) {
        Enumeration<URL> resources = TouchHomeUtils.class.getClassLoader().getResources(path);
        List<Map<String, String>> properties = new ArrayList<>();
        while (resources.hasMoreElements()) {
            try (InputStream input = resources.nextElement().openStream()) {
                Properties prop = new Properties();
                prop.load(input);
                properties.add(new HashMap(prop));
            }
        }
        return properties;
    }

    // consume file name with thymaleaf...
    public static TemplateBuilder templateBuilder(String templateName) {
        return new TemplateBuilder(templateName);
    }

    private static Path getOrCreatePath(String path) {
        return TouchHomeUtils.createDirectoriesIfNotExists(rootPath.resolve(path));
    }

    @SneakyThrows
    public static <T> T newInstance(Class<T> clazz) {
        Constructor<T> constructor = findObjectConstructor(clazz);
        if (constructor != null) {
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
        return null;
    }

    @SneakyThrows
    public static <T> Constructor<T> findObjectConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        if (parameterTypes.length > 0) {
            return clazz.getConstructor(parameterTypes);
        }
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                constructor.setAccessible(true);
                return (Constructor<T>) constructor;
            }
        }
        return null;
    }

    public static boolean isValidZipArchive(File archive) {
        if (!archive.exists() || !archive.canRead()) {
            return false;
        }
        switch (FilenameUtils.getExtension(archive.getName())) {
            case "zip":
                return new ZipFile(archive).isValidZipFile();
            case "7z":
                try {
                    new SevenZFile(archive);
                    return true;
                } catch (IOException ignored) {
                }
        }
        return false;
    }

    public static void unzip(Path file, Path destination) {
        unzip(file, destination, null, null);
    }

 /*   @SneakyThrows
    public static void tempDir(Consumer<Path> consumer) {
        Path tmpDir = rootPath.resolve("tmp_" + System.currentTimeMillis());
        Files.createDirectories(tmpDir);
        try {
            consumer.accept(tmpDir);
        } finally {
            if (!Files.deleteIfExists(tmpDir)) {
                log.error("Unable to delete tmpDir: <{}>", tmpDir);
            }
        }
    }*/

    @SneakyThrows
    public static void unzip(@NotNull Path file, @NotNull Path destination,
                             @Nullable String password, @Nullable ProgressBar progressBar) {
        if (progressBar != null) {
            progressBar.progress(0, "Unzip files. Calculate size...");
        }
        if (file.getFileName().toString().endsWith(".zip")) {
            ZipFile zipFile = new ZipFile(file.toFile());
            zipFile.extractAll(destination.toString());
        } else if (file.getFileName().toString().endsWith(".7z")) {
            double fileSize = progressBar == null ? 1D : getZipFileSize(file);

            int maxMb = (int) (fileSize / ONE_MB_BI.intValue());
            byte[] oneMBBuff = new byte[ONE_MB_BI.intValue()];
            SevenZFile sevenZFile = new SevenZFile(file.toFile(), password == null ? null : password.toCharArray());
            SevenZArchiveEntry entry;

            int nextStep = 1;
            int readBytes = 0;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                Path curFile = destination.resolve(entry.getName());
                Path parent = curFile.getParent();
                Files.createDirectories(parent);
                FileOutputStream out = new FileOutputStream(curFile.toFile());

                int bytesToRead = (int) entry.getSize();
                while (bytesToRead > 0) {
                    long bytes = Math.min(bytesToRead, ONE_MB_BI.intValue());
                    byte[] content = bytes == ONE_MB_BI.intValue() ? oneMBBuff : new byte[(int) bytes];
                    sevenZFile.read(content);
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
                out.close();
            }
            sevenZFile.close();
        }
        if (progressBar != null) {
            progressBar.progress(99, "Unzip files done.");
        }
    }

    public static long getZipFileSize(Path file) throws IOException {
        SevenZFile sevenZFile = new SevenZFile(file.toFile(), new char[0]);
        long fullSize = 0;
        for (SevenZArchiveEntry sevenZArchiveEntry : sevenZFile.getEntries()) {
            fullSize += sevenZArchiveEntry.getSize();
        }
        sevenZFile.close();
        return fullSize;
    }

    private static OsName detectOs() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return SystemUtils.OS_ARCH.equals("x64") ? OsName.Windows_x64 : OsName.Windows_x86;
        } else if (SystemUtils.IS_OS_LINUX) {
            switch (SystemUtils.OS_ARCH) {
                case "x86":
                    return OsName.Linux_x86;
                case "x64":
                    return OsName.Linux_x64;
                case "ARMv6":
                    return OsName.Linux_ARMv6;
                case "ARMv7":
                    return OsName.Linux_ARMv7;
                case "ARMv8":
                    return OsName.Linux_ARMv8;
            }
        }
        throw new RuntimeException("Unable to detect OS");
    }

    public enum OsName {
        Windows_x86,
        Windows_x64,
        Linux_x86,
        Linux_x64,
        Linux_ARMv6,
        Linux_ARMv7,
        Linux_ARMv8;

        public boolean isLinux() {
            return this.name().startsWith("Linux");
        }

        public boolean isWindows() {
            return this.name().startsWith("Windows");
        }
    }

    public static class TemplateBuilder {
        private final Context context = new Context();
        private final TemplateEngine templateEngine;
        private final String templateName;

        TemplateBuilder(String templateName) {
            this.templateName = templateName;
            this.templateEngine = new TemplateEngine();
            ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
            templateResolver.setTemplateMode(TemplateMode.HTML);
            templateEngine.setTemplateResolver(templateResolver);
        }

        public TemplateBuilder set(String key, Object value) {
            context.setVariable(key, value);
            return this;
        }

        public String build() {
            StringWriter stringWriter = new StringWriter();
            templateEngine.process("templates/" + templateName, context, stringWriter);
            return stringWriter.toString();
        }
    }
}
