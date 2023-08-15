package org.homio.api.util;

import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import com.fazecast.jSerialComm.SerialPort;
import com.pivovarit.function.ThrowingBiConsumer;
import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.homio.api.EntityContext;
import org.homio.api.fs.TreeNode;
import org.homio.api.fs.archive.ArchiveUtil;
import org.homio.api.fs.archive.ArchiveUtil.UnzipFileIssueHandler;
import org.homio.hquery.Curl;
import org.homio.hquery.ProgressBar;
import org.homio.hquery.hardware.network.NetworkHardwareRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.w3c.dom.Document;

@SuppressWarnings("unused")
@Log4j2
public class CommonUtils {

    @Getter
    private static final Path logsPath = getOrCreatePath("logs");
    @Getter
    private static final Path configPath = getOrCreatePath("conf");
    @Getter
    private static final Path filesPath = getOrCreatePath("asm_files");
    @Getter
    private static final Path installPath = getOrCreatePath("installs");
    @Getter
    private static final Path externalJarClassPath = getOrCreatePath("external_jars");
    @Getter
    private static final Path addonPath = getOrCreatePath("addons");
    @Getter
    private static final Path mediaPath = getOrCreatePath("media");
    @Getter
    private static final Path audioPath = getOrCreatePath("media/audio");
    @Getter
    private static final Path imagePath = getOrCreatePath("media/image");
    @Getter
    private static final Path sshPath = getOrCreatePath("ssh");
    @Getter
    private static final Path tmpPath = getOrCreatePath("tmp");

    // map for store different statuses
    @Getter
    private static final Map<String, AtomicInteger> statusMap = new ConcurrentHashMap<>();
    public static String MACHINE_IP_ADDRESS = "127.0.0.1";
    public static SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Set<String> specialExtensions = new HashSet<>(Arrays.asList("gz", "xz"));
    private static Path rootPath;

    public static String generateUUID() {
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
    }

    /**
     * Download archive file from url, extract, delete archive
     *
     * @param url            - url of archived source
     * @param targetFileName - target file name
     * @param progressBar    - progress bar to print progress
     * @return unarchived downloaded folder
     */
    @SneakyThrows
    public static @NotNull List<Path> downloadAndExtract(@NotNull String url, @NotNull String targetFileName,
        @NotNull ProgressBar progressBar) {
        progressBar.progress(0, format("Downloading '%s' from url '%s'", targetFileName, url));
        Path targetFolder = CommonUtils.getInstallPath();
        Path archiveFile = targetFolder.resolve(targetFileName);
        if (!Files.exists(archiveFile)) {
            Curl.downloadWithProgress(url, archiveFile, progressBar);
        }
        progressBar.progress(90, format("Extracting '%s' to path '%s'", archiveFile, targetFolder));
        List<Path> files = ArchiveUtil.unzip(archiveFile, targetFolder, null, false, progressBar, UnzipFileIssueHandler.replace);
        Files.deleteIfExists(archiveFile);
        return files;
    }

    public static String getExtension(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        if (specialExtensions.contains(extension)) {
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
        Files.walkFileTree(path, new SimpleFileVisitor<>() {

            @Override
            @SneakyThrows
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                pathHandler.accept(file);
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
                pathHandler.accept(dir);
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

        return StringUtils.defaultString(cause.getMessage(), cause.toString());
    }

    @SneakyThrows
    public static String getResourceAsString(String addonId, String resource) {
        return IOUtils.toString(getResource(addonId, resource), Charset.defaultCharset());
    }

    public static void addToListSafe(List<String> list, String value) {
        if (!value.isEmpty()) {
            list.add(value);
        }
    }

    public static Path createDirectoriesIfNotExists(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception ex) {
                log.error("Unable to create path: <{}>", path.toAbsolutePath().toString());
            }
        }
        return path;
    }

    public static Map<String, String> readPropertiesMerge(String path) {
        Map<String, String> map = new HashMap<>();
        readProperties(path).forEach(map::putAll);
        return map;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static List<String> readFile(String fileName) {
        try {
            return IOUtils.readLines(Objects.requireNonNull(CommonUtils.class.getClassLoader().getResourceAsStream(fileName)),
                Charset.defaultCharset());
        } catch (Exception ex) {
            log.error(getErrorMessage(ex), ex);

        }
        return Collections.emptyList();
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

    @SneakyThrows
    public static URL getResource(String addonID, String resource) {
        URL resourceURL = null;
        ArrayList<URL> urls = Collections.list(CommonUtils.class.getClassLoader().getResources(resource));
        if (urls.size() == 1) {
            resourceURL = urls.get(0);
        } else if (urls.size() > 1 && addonID != null) {
            resourceURL = urls.stream().filter(url -> url.getFile().contains(addonID)).findAny().orElse(null);
        }
        return resourceURL;
    }

    @SneakyThrows
    public static <T> T newInstance(Class<T> clazz) {
        Constructor<T> constructor = findObjectConstructor(clazz);
        if (constructor != null) {
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
        throw new IllegalArgumentException("Class " + clazz.getSimpleName() + " has to have empty constructor");
    }

    public static String ping(String host, int port) throws ConnectException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("google.com", port), 5000);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception ignore) {
        }
        throw new ConnectException("Host %s:%s not reachable".formatted(host, port));
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

    // consume file name with thymeleaf...
    public static TemplateBuilder templateBuilder(String templateName) {
        return new TemplateBuilder(templateName);
    }

    @SneakyThrows
    public static String toString(Document document) {
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

    @SneakyThrows
    private static List<Map<String, String>> readProperties(String path) {
        Enumeration<URL> resources = CommonUtils.class.getClassLoader().getResources(path);
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

    public static boolean deletePath(Path path) {
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

    public static void addFiles(Path tmpPath, Collection<TreeNode> files,
        BiFunction<Path, TreeNode, Path> pathResolver) {
        addFiles(tmpPath, files, pathResolver,
            (treeNode, path) -> Files.copy(treeNode.getInputStream(), path, REPLACE_EXISTING),
            (treeNode, path) -> Files.createDirectories(path));
    }

    @SneakyThrows
    public static void addFiles(Path tmpPath, Collection<TreeNode> files,
        BiFunction<Path, TreeNode, Path> pathResolver,
        ThrowingBiConsumer<TreeNode, Path, Exception> fileWriteResolver,
        ThrowingBiConsumer<TreeNode, Path, Exception> folderWriteResolver) {
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

    private static String getTimestampString(Date date) {
        return DATE_TIME_FORMAT.format(date);
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
    public static @NotNull Path getHomioPropertiesLocation() {
        Path propertiesFile = (SystemUtils.IS_OS_WINDOWS ? SystemUtils.getUserHome().toPath().resolve("homio") :
            createDirectoriesIfNotExists(Paths.get("/opt/homio"))).resolve("homio.properties");
        if (!Files.exists(propertiesFile)) {
            ApplicationHome applicationHome = new ApplicationHome();
            Path jarLocation = applicationHome.getDir().toPath();
            return jarLocation.resolve("homio.properties");
        }
        return propertiesFile;
    }

    @SneakyThrows
    public static Path getRootPath() {
        if (rootPath == null) {
            Path propertiesLocation = getHomioPropertiesLocation();
            Properties homioProperties = new Properties();
            try {
                homioProperties.load(Files.newInputStream(propertiesLocation));
                rootPath = Paths.get(homioProperties.getProperty("rootPath"));
                Files.createDirectories(rootPath);
            } catch (Exception ignore) {
                rootPath = propertiesLocation.getParent();
                homioProperties.setProperty("rootPath", rootPath.toString());
                homioProperties.store(Files.newOutputStream(propertiesLocation), null);
            }
        }
        return rootPath;
    }

    public static Path getOrCreatePath(String path) {
        return createDirectoriesIfNotExists(getRootPath().resolve(path));
    }

    public static SerialPort getSerialPort(String value) {
        return StringUtils.isEmpty(value) ? null :
            Stream.of(SerialPort.getCommPorts())
                  .filter(p -> p.getSystemPortName().equals(value)).findAny().orElse(null);
    }

    public static String splitNameToReadableFormat(@NotNull String name) {
        String[] items = name.split("_");
        return StringUtils.capitalize(String.join(" ", items));
    }

    // Simple utility for scan for ip range
    public static void scanForDevice(EntityContext entityContext, int devicePort, String deviceName,
        ThrowingFunction<String, Boolean, Exception> testDevice,
        Consumer<String> createDeviceHandler) {
        Consumer<String> deviceHandler = (ip) -> {
            try {
                if (testDevice.apply("127.0.0.1")) {
                    List<String> messages = new ArrayList<>();
                    messages.add(Lang.getServerMessage("NEW_DEVICE.GENERAL_QUESTION", deviceName));
                    messages.add(Lang.getServerMessage("NEW_DEVICE.TITLE", deviceName + "(" + ip + ":" + devicePort + ")"));
                    messages.add(Lang.getServerMessage("NEW_DEVICE.URL", ip + ":" + devicePort));
                    entityContext.ui().sendConfirmation("Confirm-" + deviceName + "-" + ip,
                        Lang.getServerMessage("NEW_DEVICE.TITLE", deviceName), () ->
                            createDeviceHandler.accept(ip), messages, "confirm-create-" + deviceName + "-" + ip);
                }
            } catch (Exception ignore) {
            }
        };

        NetworkHardwareRepository networkHardwareRepository = entityContext.getBean(NetworkHardwareRepository.class);
        String ipAddressRange = MACHINE_IP_ADDRESS.substring(0, MACHINE_IP_ADDRESS.lastIndexOf(".") + 1) + "0-255";
        deviceHandler.accept("127.0.0.1");
        networkHardwareRepository.buildPingIpAddressTasks(ipAddressRange, log::info, Collections.singleton(devicePort), 500,
            (url, port) -> deviceHandler.accept(url));
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
