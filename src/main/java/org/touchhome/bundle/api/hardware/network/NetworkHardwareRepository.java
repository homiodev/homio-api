package org.touchhome.bundle.api.hardware.network;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.hquery.api.*;
import org.touchhome.bundle.api.service.scan.BaseItemsDiscovery;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.lang.reflect.Field;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@HardwareRepositoryAnnotation(stringValueOnDisable = "N/A")
public interface NetworkHardwareRepository {
    @HardwareQuery(name = "Switch hotspot", value = "autohotspot swipe", printOutput = true)
    void switchHotSpot();

    @HardwareQuery(name = "Scan networks", value = "iwlist :iface scan")
    @ErrorsHandler(onRetCodeError = "Got some major errors from our scan command",
            notRecognizeError = "Got some errors from our scan command",
            errorHandlers = {
                    @ErrorsHandler.ErrorHandler(onError = "Device or resource busy", throwError = "Scans are overlapping; slow down putToCache frequency"),
                    @ErrorsHandler.ErrorHandler(onError = "Allocation failed", throwError = "Too many networks for iwlist to handle")
            })
    @ListParse(delimiter = ".*Cell \\d\\d.*", clazz = Network.class)
    List<Network> scan(@HQueryParam("iface") String iface);

    @HardwareQuery(name = "Network stat", value = "iwconfig :iface")
    @ErrorsHandler(onRetCodeError = "Error getting wireless devices information", errorHandlers = {})
    NetworkStat stat(@HQueryParam("iface") String iface);

    @HardwareQuery(name = "Disable network", value = "ifconfig :iface down")
    @ErrorsHandler(onRetCodeError = "There was an unknown error disabling the interface", notRecognizeError = "There was an error disabling the interface", errorHandlers = {})
    void disable(@HQueryParam("iface") String iface);

    @HardwareQuery(name = "Restart network interface", value = "/etc/init.d/networking restart", printOutput = true)
    void restartNetworkInterface();

    @HardwareQuery(name = "Enable network", value = "ifconfig :iface up")
    @ErrorsHandler(onRetCodeError = "There was an unknown error enabling the interface",
            notRecognizeError = "There was an error enabling the interface",
            errorHandlers = {
                    @ErrorsHandler.ErrorHandler(onError = "No such device", throwError = "The interface :iface does not exist."),
                    @ErrorsHandler.ErrorHandler(onError = "Allocation failed", throwError = "Too many networks for iwlist to handle")
            })
    void enable(@HQueryParam("iface") String iface);

    @HardwareQuery(name = "Connect wep", value = "iwconfig :iface essid ':essid' key :PASSWORD")
    void connect_wep(@HQueryParam("iface") String iface, @HQueryParam("essid") String essid, @HQueryParam("password") String password);

    @ErrorsHandler(onRetCodeError = "Shit is broken TODO", errorHandlers = {})
    @HardwareQuery(name = "Connect wpa", value = "wpa_passphrase ':essid' ':password' > wpa-temp.conf && sudo wpa_supplicant -D wext -i :iface -c wpa-temp.conf && rm wpa-temp.conf")
    void connect_wpa(@HQueryParam("iface") String iface, @HQueryParam("essid") String essid, @HQueryParam("password") String password);

    @HardwareQuery(name = "Connect open", value = "iwconfig :iface essid ':essid'")
    void connect_open(@HQueryParam("iface") String iface, @HQueryParam("essid") String essid);

    @HardwareQuery(name = "Get network description", value = "ifconfig :iface", ignoreOnError = true)
    NetworkDescription getNetworkDescription(@HQueryParam("iface") String iface);

    @HardwareQuery(name = "Get wifi password", value = "grep -r 'psk=' /etc/wpa_supplicant/wpa_supplicant.conf | cut -d = -f 2 | cut -d \\\" -f 2")
    String getWifiPassword();

    @HardwareQuery(name = "Get gateway ip address", value = "netstat -nr", win = "netstat -nr", cacheValid = 3600, ignoreOnError = true, valueOnError = "n/a")
    @RawParse(nix = NetStatGatewayParser.class, win = NetStatGatewayParser.class)
    String getGatewayIpAddress();

    @CurlQuery(value = "http://checkip.amazonaws.com", cacheValid = 3600, ignoreOnError = true,
            mapping = TrimEndMapping.class, valueOnError = "127.0.0.1")
    String getOuterIpAddress();

    @CurlQuery(value = "http://ip-api.com/json/:ip", cache = true, ignoreOnError = true)
    IpGeoLocation getIpGeoLocation(@HQueryParam("ip") String ip);

    @CurlQuery(value = "https://geocode.xyz/:city?json=1", cache = true, ignoreOnError = true)
    CityToGeoLocation findCityGeolocation(@HQueryParam("city") String city);

    default CityToGeoLocation findCityGeolocationOrThrowException(String city) {
        CityToGeoLocation cityGeolocation = findCityGeolocation(city);
        if (cityGeolocation.error != null) {
            String error = cityGeolocation.error.description;
            if ("15. Your request did not produce any results.".equals(error)) {
                error = "Unable to find city: " + city + ". Please, check city from site: https://geocode.xyz";
            }
            throw new IllegalArgumentException(error);
        }
        return cityGeolocation;
    }

    default Map<String, Callable<Integer>> buildPingIpAddressTasks(Logger log, Set<Integer> ports, int timeout, BiConsumer<String, Integer> handler) {
        String gatewayIpAddress = getGatewayIpAddress();
        if (gatewayIpAddress == null) {
            throw new IllegalStateException("Unable to proceed due ip address not found. Please check you connected to Router");
        }
        Map<String, Callable<Integer>> tasks = new HashMap<>();
        String scanIp = gatewayIpAddress.substring(0, gatewayIpAddress.lastIndexOf(".") + 1);

        for (Integer port : ports) {
            log.info("Checking ip address {}:{}", gatewayIpAddress, port);
            for (int i = 0; i < 255; i++) {
                int ipSuffix = i;
                tasks.put("check-ip-" + ipSuffix + "-port-" + port, () -> {
                    String ipAddress = scanIp + ipSuffix;
                    log.debug("Check ip: {}:{}", ipAddress, port);
                    if (pingAddress(ipAddress, port, timeout)) {
                        handler.accept(ipAddress, port);
                        return ipSuffix;
                    }
                    return null;
                });
            }
        }
        return tasks;
    }

    default boolean pingAddress(String ipAddress, int port, int timeout) {
        try {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ipAddress, port), timeout);
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @SneakyThrows
    default String getIPAddress() {
        if (SystemUtils.IS_OS_LINUX) {
            return getNetworkDescription().getInet();
        }

        String ipAddress = null;
        try {
            for (Enumeration<NetworkInterface> enumNetworks = NetworkInterface.getNetworkInterfaces(); enumNetworks
                    .hasMoreElements(); ) {
                NetworkInterface networkInterface = enumNetworks.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr
                        .hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().length() < 18
                            && inetAddress.isSiteLocalAddress()) {
                        ipAddress = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {
        }
        return ipAddress;
    }

    @SneakyThrows
    default void setWifiCredentials(String ssid, String password, String country) {
        String value = TouchHomeUtils.templateBuilder("wpa_supplicant.conf")
                .set("SSID", ssid).set("PASSWORD", password).set("COUNTRY", country)
                .build();

        Files.write(Paths.get("/etc/wpa_supplicant/wpa_supplicant.conf"), value.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    }

    @HardwareQuery(name = "Get active network interface", value = "ip addr | awk '/state UP/ {print $2}' | sed 's/.$//'")
    String getActiveNetworkInterface();

    @HardwareQuery(name = "Set wifi power save off", value = "iw :iface set power_save off")
    void setWifiPowerSaveOff(@HQueryParam("iface") String iface);

    @HardwareQuery(name = "Check ssh keys exists", value = "test -f ~/.ssh/id_rsa", cacheValid = 3600)
    boolean isSshGenerated();

    @HardwareQuery(name = "Generate ssh keys", value = "cat /dev/zero | ssh-keygen -q -N \"\"")
    void generateSSHKeys();

    default NetworkDescription getNetworkDescription() {
        return !EntityContext.isLinuxEnvironment() ? null :
                getNetworkDescription(getActiveNetworkInterface());
    }

    @Getter
    class CityToGeoLocation {
        private String longt;
        private String latt;
        private Error error;

        @Setter
        private static class Error {
            private String description;
        }
    }

    class NetStatGatewayParser implements RawParse.RawParseHandler {

        @Override
        public Object handle(List<String> inputs, Field field) {
            String ipString = inputs.stream().filter(i -> i.contains("0.0.0.0")).findAny().orElse(null);
            if (ipString != null) {
                List<String> list = Stream.of(ipString.split(" ")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                if (Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$").matcher(list.get(2)).matches()) {
                    return list.get(2);
                }
            }
            return null;
        }
    }

    @Getter
    class IpGeoLocation {
        private String country = "unknown";
        private String countryCode = "unknown";
        private String region = "unknown";
        private String regionName = "unknown";
        private String city = "unknown";
        private Integer lat = 0;
        private Integer lon = 0;
        private String timezone = "unknown";

        @Override
        public String toString() {
            return new JSONObject(this).toString();
        }
    }

    class TrimEndMapping implements Function<Object, Object> {

        @Override
        public Object apply(Object o) {
            return ((String) o).trim().replaceAll("\n", "");
        }
    }
}
