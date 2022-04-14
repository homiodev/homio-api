package org.touchhome.bundle.api.util;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.common.util.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class TouchHomeUtils {

    public static final String PRIMARY_COLOR = "#E65100";
    public static final Path TMP_FOLDER = Paths.get(FileUtils.getTempDirectoryPath());
    public static Map<String, Pair<Status, String>> STATUS_MAP = new ConcurrentHashMap<>();

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
    private static final Path audioPath;
    @Getter
    private static final Path imagePath;

    @Getter
    private static final Path sshPath;

    public static String MACHINE_IP_ADDRESS = "127.0.0.1";
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    @Getter
    private static Path rootPath;

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
        imagePath = getOrCreatePath("media/image");
        audioPath = getOrCreatePath("media/audio");
    }

    public static JSONObject putOpt(JSONObject jsonObject, String key, Object value) {
        if (StringUtils.isNotEmpty(key) && value != null) {
            jsonObject.put(key, value);
        }
        return jsonObject;
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

    public static Path path(String path) {
        return rootPath.resolve(path);
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

    private static Path getOrCreatePath(String path) {
        return CommonUtils.createDirectoriesIfNotExists(rootPath.resolve(path));
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


    public static class Colors {
        public static final String RED = "#BD3500";
        public static final String GREEN = "#17A328";
    }
}
