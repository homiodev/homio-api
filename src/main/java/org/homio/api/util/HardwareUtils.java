package org.homio.api.util;

import com.fazecast.jSerialComm.SerialPort;
import com.pivovarit.function.ThrowingFunction;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.homio.api.EntityContext;
import org.homio.hquery.hardware.network.NetworkHardwareRepository;
import org.homio.hquery.hardware.other.MachineHardwareRepository;
import org.jetbrains.annotations.NotNull;

@Log4j2
public class HardwareUtils {

    public static String MACHINE_IP_ADDRESS = "127.0.0.1";	/**
     * Loads native library from the jar file (storing it in the temp dir)
     * @param library JNI library name
     */
    public static void loadLibrary(String library) {
        String filename = System.mapLibraryName(library);
        String fullFilename = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + filename;
        try {
            // try to load from the temp dir (in case it is already there)
            System.load(fullFilename);
        }
        catch (UnsatisfiedLinkError err2) {
            try {
                // try to extract from the jar
                File targetFile;
                try (InputStream is = HardwareUtils.class.getClassLoader().getResourceAsStream(filename)) {
                    if (is == null) {
                        throw new IOException(filename + " not found in the jar file (classpath)");
                    }
                    targetFile = new File(fullFilename);
                    FileUtils.copyToFile(is, targetFile);
                }
                targetFile.setExecutable(true, false);
                System.load(fullFilename);
            }
            catch (IOException ioe) {
                throw new RuntimeException("Unable to extract native library: " + library, ioe);
            }
        }
    }

    public static SerialPort getSerialPort(String value) {
        return StringUtils.isEmpty(value) ? null :
            Stream.of(SerialPort.getCommPorts())
                  .filter(p -> p.getSystemPortName().equals(value)).findAny().orElse(null);
    }

    public static String ping(String host, int port) throws ConnectException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("google.com", port), 5000);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception ignore) {
        }
        throw new ConnectException("Host %s:%s not reachable".formatted(host, port));
    }

    public static @NotNull Architecture getArchitecture(@NotNull EntityContext entityContext) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return Architecture.win64;
        }
        String architecture = entityContext.getBean(MachineHardwareRepository.class).getMachineInfo().getArchitecture();
        if (architecture.startsWith("armv6")) {
            return Architecture.arm32v6;
        } else if (architecture.startsWith("armv7")) {
            return Architecture.arm32v7;
        } else if (architecture.startsWith("armv8")) {
            return Architecture.arm32v8;
        } else if (architecture.startsWith("aarch64")) {
            return Architecture.aarch64;
        } else if (architecture.startsWith("x86_64")) {
            return Architecture.amd64;
        }
        throw new IllegalStateException("Unable to find architecture: " + architecture);
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
                    entityContext.ui().dialog().sendConfirmation("Confirm-" + deviceName + "-" + ip,
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

    @RequiredArgsConstructor
    public enum Architecture {
        arm32v6(s -> s.contains("arm32v6")),
        arm32v7(s -> s.contains("arm32v7")),
        arm32v8(s -> s.contains("arm32v8")),
        aarch64(s -> s.contains("arm64")),
        amd64(s -> s.contains("amd64")),
        win64(s -> s.contains("windows"));

        public final Predicate<String> matchName;
    }
}
