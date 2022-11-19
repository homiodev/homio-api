package org.touchhome.bundle.api.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fazecast.jSerialComm.SerialPort;
import com.pivovarit.function.ThrowingFunction;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.tika.Tika;
import org.json.JSONObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.RestartHandlerOnChange;
import org.touchhome.bundle.api.hardware.network.NetworkHardwareRepository;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.common.util.CommonUtils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.*;
import static org.touchhome.common.util.Lang.getServerMessage;

@Log4j2
public class TouchHomeUtils {

    public static final String APP_UUID;
    public static final int RUN_COUNT;
    public static final Tika TIKA = new Tika();
    @Getter
    private static final Path configPath = getOrCreatePath("conf");
    @Getter
    private static final Path filesPath = getOrCreatePath("asm_files");
    @Getter
    private static final Path installPath = getOrCreatePath("installs");
    @Getter
    private static final Path externalJarClassPath = getOrCreatePath("external_jars");
    @Getter
    private static final Path bundlePath = getOrCreatePath("bundles");
    @Getter
    private static final Path mediaPath = getOrCreatePath("media");
    @Getter
    private static final Path audioPath = getOrCreatePath("media/audio");
    @Getter
    private static final Path imagePath = getOrCreatePath("media/image");
    @Getter
    private static final Path sshPath = getOrCreatePath("ssh");
    // map for store different statuses
    @Getter
    private static final Map<String, AtomicInteger> statusMap = new ConcurrentHashMap<>();
    public static String MACHINE_IP_ADDRESS = "127.0.0.1";
    public static SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    static {
        ConfFile confFile = readConfigurationFile();
        APP_UUID = confFile.getUuid();
        RUN_COUNT = confFile.getRunCount();
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
        return CommonUtils.getRootPath().resolve(path);
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
        Path relativePath = Paths.get(CommonUtils.getRootPath().toString(), path);
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

    public static Path getOrCreatePath(String path) {
        return CommonUtils.createDirectoriesIfNotExists(CommonUtils.getRootPath().resolve(path));
    }

    @SneakyThrows
    public static boolean isRequireRestartHandler(Object oldEntity, Object newEntity) {
        if (oldEntity == null) { // in case if just created
            return false;
        }
        Method[] methods = MethodUtils.getMethodsWithAnnotation(newEntity.getClass(), RestartHandlerOnChange.class, true, false);
        for (Method method : methods) {
            Object newValue = MethodUtils.invokeMethod(newEntity, method.getName());
            Object oldValue = MethodUtils.invokeMethod(oldEntity, method.getName());
            if (!Objects.equals(newValue, oldValue)) {
                return true;
            }
        }
        return false;
    }

    public static SerialPort getSerialPort(String value) {
        return StringUtils.isEmpty(value) ? null :
                Stream.of(SerialPort.getCommPorts())
                        .filter(p -> p.getSystemPortName().equals(value)).findAny().orElse(null);
    }

    public static boolean isValidJson(String json) {
        try {
            new JSONObject(json);
        } catch (Exception ignore) {
            return false;
        }
        return true;
    }

    /**
     * Simple utility for scan for ip range
     */
    public static void scanForDevice(EntityContext entityContext, int devicePort, String deviceName,
                                     ThrowingFunction<String, Boolean, Exception> testDevice,
                                     Consumer<String> createDeviceHandler) {
        Consumer<String> deviceHandler = (ip) -> {
            try {
                if (testDevice.apply("127.0.0.1")) {
                    List<String> messages = new ArrayList<>();
                    messages.add(getServerMessage("NEW_DEVICE.GENERAL_QUESTION", "NAME", deviceName));
                    messages.add(getServerMessage("NEW_DEVICE.TITLE", "NAME", deviceName + "(" + ip + ":" + devicePort + ")"));
                    messages.add(getServerMessage("NEW_DEVICE.URL", "URL", ip + ":" + devicePort));
                    entityContext.ui().sendConfirmation("Confirm-" + deviceName + "-" + ip,
                            getServerMessage("NEW_DEVICE.TITLE", "NAME", deviceName), () ->
                                    createDeviceHandler.accept(ip), messages, "confirm-create-" + deviceName + "-" + ip);
                }
            } catch (Exception ignore) {
            }
        };

        NetworkHardwareRepository networkHardwareRepository = entityContext.getBean(NetworkHardwareRepository.class);
        String ipAddressRange = MACHINE_IP_ADDRESS.substring(0, MACHINE_IP_ADDRESS.lastIndexOf(".") + 1) + "0-255";
        deviceHandler.accept("127.0.0.1");
        networkHardwareRepository.buildPingIpAddressTasks(ipAddressRange, log, Collections.singleton(devicePort), 500,
                (url, port) -> deviceHandler.accept(url));
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
    private static ConfFile readConfigurationFile() {
        Path confFilePath = CommonUtils.getRootPath().resolve("touchhome.conf");
        ConfFile confFile = null;
        if (Files.exists(confFilePath)) {
            try {
                confFile = CommonUtils.OBJECT_MAPPER.readValue(confFilePath.toFile(), ConfFile.class);
            } catch (Exception ex) {
                log.error("Found corrupted config file. Regenerate new one.");
            }
        }
        if (confFile == null) {
            confFile = new ConfFile().setRunCount(0).setUuid(String.valueOf(System.currentTimeMillis()));
        }
        confFile.setRunCount(confFile.getRunCount() + 1);
        CommonUtils.OBJECT_MAPPER.writeValue(confFilePath.toFile(), confFile);
        return confFile;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    private static class ConfFile {
        private String uuid;
        @JsonProperty("run_count")
        private int runCount;
    }
}
