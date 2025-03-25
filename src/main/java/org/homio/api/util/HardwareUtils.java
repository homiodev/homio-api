package org.homio.api.util;

import com.fazecast.jSerialComm.SerialPort;
import com.pivovarit.function.ThrowingFunction;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.Context;
import org.homio.hquery.hardware.network.NetworkHardwareRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Log4j2
public class HardwareUtils {

  public static String MACHINE_IP_ADDRESS = "127.0.0.1";
  public static String APP_ID;
  public static int RUN_COUNT;

  /**
   * Loads native library from the jar file (storing it in the temp dir)
   * @param library JNI library name
   */
  public static void loadLibrary(@NotNull String library) {
    String filename = System.mapLibraryName(library);
    String fullFilename = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + filename;
    try {
      // try to load from the temp dir (in case it is already there)
      System.load(fullFilename);
    } catch (UnsatisfiedLinkError err2) {
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
      } catch (IOException ioe) {
        throw new RuntimeException("Unable to extract native library: " + library, ioe);
      }
    }
  }

  public static SerialPort getSerialPort(@Nullable String value) {
    return StringUtils.isEmpty(value) ? null :
      Stream.of(SerialPort.getCommPorts())
        .filter(p -> p.getSystemPortName().equals(value)).findAny().orElse(null);
  }

  // Simple utility for scan for ip range
  public static void scanForDevice(@NotNull Context context, int devicePort,
                                   @NotNull String deviceName,
                                   @NotNull ThrowingFunction<String, Boolean, Exception> testDevice,
                                   @NotNull Consumer<String> createDeviceHandler) {
    Consumer<String> deviceHandler = (ip) -> {
      try {
        if (testDevice.apply("127.0.0.1")) {
          List<String> messages = new ArrayList<>();
          messages.add(Lang.getServerMessage("NEW_DEVICE.GENERAL_QUESTION", deviceName));
          messages.add(Lang.getServerMessage("NEW_DEVICE.TITLE", deviceName + "(" + ip + ":" + devicePort + ")"));
          messages.add(Lang.getServerMessage("NEW_DEVICE.URL", ip + ":" + devicePort));
          context.ui().dialog().sendConfirmation("Confirm-" + deviceName + "-" + ip,
            Lang.getServerMessage("NEW_DEVICE.TITLE", deviceName), () ->
              createDeviceHandler.accept(ip), messages, "confirm-create-" + deviceName + "-" + ip);
        }
      } catch (Exception ignore) {
      }
    };

    NetworkHardwareRepository networkHardwareRepository = context.getBean(NetworkHardwareRepository.class);
    String ipAddressRange = MACHINE_IP_ADDRESS.substring(0, MACHINE_IP_ADDRESS.lastIndexOf(".") + 1) + "0-255";
    deviceHandler.accept("127.0.0.1");
    networkHardwareRepository.buildPingIpAddressTasks(ipAddressRange, log::info, Collections.singleton(devicePort), 500,
      (url, port) -> deviceHandler.accept(url));
  }
}
