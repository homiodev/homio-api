package org.homio.api.util;

import com.pivovarit.function.ThrowingBiConsumer;
import com.pivovarit.function.ThrowingConsumer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.tika.Tika;
import org.homio.api.fs.TreeNode;
import org.homio.api.repository.GitHubProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;

@Getter
@Log4j2
public final class CommonUtils {

    // map for store different statuses
    private static final @Getter Map<String, AtomicInteger> statusMap = new ConcurrentHashMap<>();
    public static SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Path rootPath;
    private static final @Getter Path logsPath = getOrCreatePath("logs");
    private static final @Getter Path logsEntitiesPath = getOrCreatePath("logs/entities");
    private static final @Getter Path configPath = getOrCreatePath("conf");
    private static final @Getter Path filesPath = getOrCreatePath("asm_files");
    private static final @Getter Path installPath = getOrCreatePath("installs");
    private static final @Getter Path externalJarClassPath = getOrCreatePath("external_jars");
    private static final @Getter Path addonPath = getOrCreatePath("addons");
    private static final @Getter Path mediaPath = getOrCreatePath("media");
    private static final @Getter Path audioPath = getOrCreatePath("media/audio");
    private static final @Getter Path imagePath = getOrCreatePath("media/image");
    private static final @Getter Path sshPath = getOrCreatePath("ssh");
    private static final @Getter Path tmpPath = getOrCreatePath("tmp");

    public static final Tika TIKA = new Tika();
    public static GitHubProject STATIC_FILES = GitHubProject.of("homiodev", "static-files");

    public static String generateUUID() {
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
    }

    public static String generateShortUUID(int count) {
        int byteLength = (int) Math.ceil(count * 3.0 / 4.0);
        byte[] randomBytes = new byte[byteLength];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes).substring(0, count);
    }

    public static String getExtension(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        if (List.of("gz", "xz").contains(extension)) {
            if (fileName.endsWith(".tar." + extension)) {
                return "tar." + extension;
            }
        }
        return extension;
    }

    public static Set<Path> removeFileOrDirectory(Path path) {
        Set<Path> removedItems = new HashSet<>();
        walkFileOrDirectory(path, item -> {
            if (Files.deleteIfExists(item)) {
                removedItems.add(item);
            }
        });
        return removedItems;
    }

    @SneakyThrows
    public static void walkFileOrDirectory(Path path, ThrowingConsumer<Path, Exception> pathHandler) {
        if (!Files.isDirectory(path)) {
            if (Files.exists(path)) {
                pathHandler.accept(path);
            }
            return;
        }
        Set<Path> visitedFiles = new HashSet<>();
        Files.walkFileTree(path, new SimpleFileVisitor<>() {

            @Override
            @SneakyThrows
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (visitedFiles.add(file)) {
                    pathHandler.accept(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            @SneakyThrows
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                pathHandler.accept(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            @SneakyThrows
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                if (exc != null) {
                    throw exc;
                }
                if (visitedFiles.add(dir)) {
                    pathHandler.accept(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static String getErrorMessage(Throwable ex) {
        if (ex == null) {
            return null;
        }
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        if (cause instanceof NullPointerException) {
            log.error("Unexpected NPE: <{}>", ex.getMessage(), ex);
            return "Unexpected NullPointerException at line: " + ex.getStackTrace()[0].toString();
        }
        if (cause instanceof UnknownHostException) {
            return "UnknownHost: " + cause.getMessage();
        }
        if (cause instanceof NoSuchFileException) {
            return "File not found: " + cause.getMessage();
        }

        return Objects.toString(cause.getMessage(), cause.toString());
    }

    @SneakyThrows
    public static String getResourceAsString(String addonId, String resource) {
        return IOUtils.toString(getResource(addonId, resource), Charset.defaultCharset());
    }

    public static void addToListSafe(@NotNull List<String> list, @Nullable String value) {
        if (StringUtils.isNotEmpty(value)) {
            list.add(value);
        }
    }

    public static @NotNull Path createDirectoriesIfNotExists(@NotNull Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception ex) {
                log.error("Unable to create path: <{}>", path.toAbsolutePath().toString());
            }
        }
        return path;
    }

    public static @NotNull Map<String, String> readPropertiesMerge(@NotNull String path) {
        Map<String, String> map = new HashMap<>();
        readProperties(path).forEach(map::putAll);
        return map;
    }

    public static @NotNull <T> Predicate<T> distinctByKey(@NotNull Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static @NotNull List<String> readFile(@NotNull String fileName) {
        try {
            try (InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
                return IOUtils.readLines(Objects.requireNonNull(resource), Charset.defaultCharset());
            }
        } catch (Exception ex) {
            log.error(getErrorMessage(ex), ex);

        }
        return Collections.emptyList();
    }

    @SneakyThrows
    public static @NotNull FileSystem getOrCreateNewFileSystem(@Nullable String fileSystemPath) {
        if (fileSystemPath == null) {
            return FileSystems.getDefault();
        }
        try {
            return FileSystems.getFileSystem(URI.create(fileSystemPath));
        } catch (Exception ex) {
            return FileSystems.newFileSystem(URI.create(fileSystemPath), Collections.emptyMap());
        }
    }

    @SneakyThrows
    public static @Nullable URL getResource(@Nullable String addonID, @NotNull String resource) {
        URL resourceURL = null;
        ArrayList<URL> urls = Collections.list(Thread.currentThread().getContextClassLoader().getResources(resource));
        if (urls.size() == 1) {
            resourceURL = urls.get(0);
        } else if (urls.size() > 1 && addonID != null) {
            resourceURL = urls.stream().filter(url -> url.getFile().contains(addonID)).findAny().orElse(null);
        }
        return resourceURL;
    }

    @SneakyThrows
    public static <T> @NotNull T newInstance(@NotNull Class<T> clazz, Object... parameters) {
        Constructor<T> constructor = findObjectConstructor(clazz, ClassUtils.toClass(parameters));
        if (constructor != null) {
            return constructor.newInstance(parameters);
        }
        throw new IllegalArgumentException("Class " + clazz.getSimpleName() + " has to have empty constructor");
    }

    /**
     * Find constructor. Not well implemented because not find fine-grain constructor. But satisfy app requirements
     *
     * @param clazz          class
     * @param parameterTypes - types
     * @param <T>            - object type
     * @return constructor or null
     */
    @SneakyThrows
    public static <T> @Nullable Constructor<T> findObjectConstructor(@NotNull Class<T> clazz, Class<?>... parameterTypes) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (isMatchConstructor(constructor, parameterTypes)) {
                constructor.setAccessible(true);
                return (Constructor<T>) constructor;
            }
        }
        return null;
    }

    public static boolean isMatchConstructor(Constructor<?> constructor, Class<?>[] parameterTypes) {
        if (parameterTypes.length != constructor.getParameterCount()) {
            return false;
        }
        for (int i = 0; i < constructor.getParameterCount(); i++) {
            Class<?> constructorParameterType = constructor.getParameterTypes()[i];
            if (!constructor.getParameters()[i].getType().isAssignableFrom(parameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    // consume file name with thymeleaf...
    public static @NotNull TemplateBuilder templateBuilder(@NotNull String templateName) {
        return new TemplateBuilder(templateName);
    }

    @SneakyThrows
    public static @NotNull String toString(@NotNull Document document) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
        return out.toString();
    }

    public static boolean deletePath(@Nullable Path path) {
        if (path == null) {
            return false;
        }
        try {
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    FileUtils.deleteDirectory(path.toFile());
                } else {
                    Files.delete(path);
                }
                return true;
            }
        } catch (IOException ex) {
            log.error("Unable to delete directory: <{}>", path, ex);
        }
        return false;
    }

    public static void addFiles(@NotNull Path tmpPath, @NotNull Collection<TreeNode> files,
                                @NotNull BiFunction<Path, TreeNode, Path> pathResolver) {
        addFiles(tmpPath, files, pathResolver,
                (treeNode, path) -> Files.copy(treeNode.getInputStream(), path, REPLACE_EXISTING),
                (treeNode, path) -> Files.createDirectories(path));
    }

    @SneakyThrows
    public static void addFiles(@NotNull Path tmpPath, @Nullable Collection<TreeNode> files,
                                @NotNull BiFunction<Path, TreeNode, Path> pathResolver,
                                @NotNull ThrowingBiConsumer<TreeNode, Path, Exception> fileWriteResolver,
                                @NotNull ThrowingBiConsumer<TreeNode, Path, Exception> folderWriteResolver) {
        if (files != null) {
            for (TreeNode treeNode : files) {
                Path filePath = pathResolver.apply(tmpPath, treeNode);
                if (!treeNode.getAttributes().isDir()) {
                    fileWriteResolver.accept(treeNode, filePath);
                } else {
                    folderWriteResolver.accept(treeNode, filePath);
                    addFiles(filePath, treeNode.getChildren(true), pathResolver, fileWriteResolver,
                            folderWriteResolver);
                }
            }
        }
    }

    public static ResponseEntity<InputStreamResource> inputStreamToResource(
            @NotNull InputStream stream,
            @NotNull MediaType contentType,
            @Nullable HttpHeaders headers) {
        try {
            return ResponseEntity.ok()
                    .contentLength(stream.available())
                    .contentType(contentType)
                    .headers(headers)
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static String getTimestampString() {
        return getTimestampString(new Date());
    }

    public static List<Date> range(Date minDate, Date maxDate) {
        long time = (maxDate.getTime() - minDate.getTime()) / 10;
        List<Date> dates = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            dates.add(new Date(minDate.getTime() + time * i));
        }
        return dates;
    }

    public static Path path(String path) {
        return getRootPath().resolve(path);
    }

    @SneakyThrows
    public static Path writeToFile(Path path, String content, boolean append) {
        return writeToFile(path, content.getBytes(StandardCharsets.UTF_8), append);
    }

    @SneakyThrows
    public static Path writeToFile(Path path, byte[] content, boolean append) {
        if (append) {
            Files.write(path, content, CREATE, WRITE, APPEND);
        } else {
            Files.write(path, content, CREATE, WRITE, TRUNCATE_EXISTING);
        }
        return path;
    }

    @SneakyThrows
    public static Path writeToFile(Path path, InputStream stream, boolean append) {
        if (append) {
            Files.copy(stream, path);
        } else {
            Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
        }
        return path;
    }

    public static Path resolvePath(String... path) {
        Path relativePath = Paths.get(getRootPath().toString(), path);
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
    public static Path getRootPath() {
        if (rootPath == null) {
            String sysRootPath = System.getProperty("rootPath");
            if (StringUtils.isEmpty(sysRootPath)) {
                throw new IllegalAccessException("System property 'rootPath' must be specified");
            } else {
                rootPath = Paths.get(sysRootPath);
            }
        }
        return rootPath;
    }

    public static Path getOrCreatePath(String path) {
        return createDirectoriesIfNotExists(getRootPath().resolve(path));
    }

    public static String splitNameToReadableFormat(@NotNull String name) {
        String[] items = name.split("_");
        if (items.length == 1) {
            name = name.replaceAll(
                    format("%s|%s|%s",
                            "(?<=[A-Z])(?=[A-Z][a-z])",
                            "(?<=[a-z])(?=[A-Z])",
                            "(?<=[A-Za-z])(?=[0-9])"
                    ), "_"
            ).toLowerCase();
        }
        items = name.split("_");
        return StringUtils.capitalize(String.join(" ", items));
    }

    public static Method findMethodByName(Class clz, String name) {
        String capitalizeName = StringUtils.capitalize(name);
        Method method = MethodUtils.getAccessibleMethod(clz, "get" + capitalizeName);
        if (method == null) {
            method = MethodUtils.getAccessibleMethod(clz, "is" + capitalizeName);
        }
        return method;
    }

    @SneakyThrows
    private static List<Map<String, String>> readProperties(String path) {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
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

    private static String getTimestampString(Date date) {
        return DATE_TIME_FORMAT.format(date);
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
