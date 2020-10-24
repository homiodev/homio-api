package org.touchhome.bundle.api.hardware.wifi;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.hquery.api.*;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.lang.reflect.Field;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@HardwareRepositoryAnnotation(stringValueOnDisable = "N/A")
public interface WirelessHardwareRepository {
    @HardwareQuery(echo = "Switch hotspot", value = "autohotspot swipe", printOutput = true)
    void switchHotSpot();

    @HardwareQuery("iwlist :iface scan")
    @ErrorsHandler(onRetCodeError = "Got some major errors from our scan command",
            notRecognizeError = "Got some errors from our scan command",
            errorHandlers = {
                    @ErrorsHandler.ErrorHandler(onError = "Device or resource busy", throwError = "Scans are overlapping; slow down putToCache frequency"),
                    @ErrorsHandler.ErrorHandler(onError = "Allocation failed", throwError = "Too many networks for iwlist to handle")
            })
    @ListParse(delimiter = ".*Cell \\d\\d.*", clazz = Network.class)
    List<Network> scan(@HQueryParam("iface") String iface);

    @HardwareQuery("iwconfig :iface")
    @ErrorsHandler(onRetCodeError = "Error getting wireless devices information", errorHandlers = {})
    NetworkStat stat(@HQueryParam("iface") String iface);

    @HardwareQuery("ifconfig :iface down")
    @ErrorsHandler(onRetCodeError = "There was an unknown error disabling the interface", notRecognizeError = "There was an error disabling the interface", errorHandlers = {})
    void disable(@HQueryParam("iface") String iface);

    @HardwareQuery(echo = "Restart network interface", value = "/etc/init.d/networking restart", printOutput = true)
    void restartNetworkInterface();

    @HardwareQuery("ifconfig :iface up")
    @ErrorsHandler(onRetCodeError = "There was an unknown error enabling the interface",
            notRecognizeError = "There was an error enabling the interface",
            errorHandlers = {
                    @ErrorsHandler.ErrorHandler(onError = "No such device", throwError = "The interface :iface does not exist."),
                    @ErrorsHandler.ErrorHandler(onError = "Allocation failed", throwError = "Too many networks for iwlist to handle")
            })
    void enable(@HQueryParam("iface") String iface);

    @HardwareQuery("iwconfig :iface essid ':essid' key :PASSWORD")
    void connect_wep(@HQueryParam("iface") String iface, @HQueryParam("essid") String essid, @HQueryParam("password") String password);

    @ErrorsHandler(onRetCodeError = "Shit is broken TODO", errorHandlers = {})
    @HardwareQuery("wpa_passphrase ':essid' ':password' > wpa-temp.conf && sudo wpa_supplicant -D wext -i :iface -c wpa-temp.conf && rm wpa-temp.conf")
    void connect_wpa(@HQueryParam("iface") String iface, @HQueryParam("essid") String essid, @HQueryParam("password") String password);

    @HardwareQuery("iwconfig :iface essid ':essid'")
    void connect_open(@HQueryParam("iface") String iface, @HQueryParam("essid") String essid);

    @HardwareQuery(value = "ifconfig :iface", ignoreOnError = true)
    NetworkDescription getNetworkDescription(@HQueryParam("iface") String iface);

    @HardwareQuery("grep -r 'psk=' /etc/wpa_supplicant/wpa_supplicant.conf | cut -d = -f 2 | cut -d \\\" -f 2")
    String getWifiPassword();

    @HardwareQuery(value = "netstat -nr", win = "netstat -nr", cacheValid = 3600, ignoreOnError = true, valueOnError = "n/a")
    @RawParse(value = NetStatGatewayParser.class)
    String getGatewayIpAddress();

    @CurlQuery(value = "http://checkip.amazonaws.com", cacheValid = 3600, ignoreOnError = true, mapping = TrimEndMapping.class)
    String getOuterIpAddress();

    @CurlQuery(value = "http://ip-api.com/json/:ip", cache = true)
    IpGeoLocation getIpGeoLocation(@HQueryParam("ip") String ip);

    @HardwareQuery("hostname -i")
    String getLinuxIPAddress();

    @CurlQuery(value = "https://geocode.xyz/:city?json=1", cache = true)
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
            return getLinuxIPAddress();
        }
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface networkInterface : Collections.list(nets)) {
            for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                if (inetAddress.isSiteLocalAddress()) {
                    return inetAddress.getHostAddress();
                }
            }
        }
        return null;
    }

    @SneakyThrows
    default void setWifiCredentials(String ssid, String password, String country) {
        String value = TouchHomeUtils.templateBuilder("wpa_supplicant.conf")
                .set("SSID", ssid).set("PASSWORD", password).set("COUNTRY", country)
                .build();

        Files.write(Paths.get("/etc/wpa_supplicant/wpa_supplicant.conf"), value.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    }

    @HardwareQuery("ip addr | awk '/state UP/ {print $2}' | sed 's/.$//'")
    String getActiveNetworkInterface();

    @HardwareQuery(echo = "Set wifi power save off", value = "iw :iface set power_save off")
    void setWifiPowerSaveOff(@HQueryParam("iface") String iface);

    @HardwareQuery(echo = "Check ssh keys exists", value = "test -f ~/.ssh/id_rsa", cache = true)
    boolean isSshGenerated();

    @HardwareQuery(echo = "Generate ssh keys", value = "cat /dev/zero | ssh-keygen -q -N \"\"")
    void generateSSHKeys();

    default boolean hasInternetAccess(String spec) {
        try {
            URL url = new URL(spec);
            URLConnection connection = url.openConnection();
            connection.connect();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    default NetworkDescription getNetworkDescription() {
        return !EntityContext.isLinuxEnvironment() ? null :
                getNetworkDescription(getActiveNetworkInterface());
    }

    class NetStatGatewayParser implements RawParse.RawParseHandler {

        @Override
        public Object handle(List<String> inputs, Field field) {
            String ipString = inputs.stream().filter(i -> i.contains("0.0.0.0")).findAny().orElse(null);
            if (ipString != null) {
                List<String> list = Stream.of(ipString.split(" ")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                return list.get(2);
            }
            return null;
        }
    }

    @Getter
    class IpGeoLocation {
        private String country;
        private String countryCode;
        private String region;
        private String regionName;
        private String city;
        private Integer lat;
        private Integer lon;
        private String timezone;

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
