package org.homio.api;

import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.function.BiConsumer;
import javax.jmdns.ServiceInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.homio.hquery.hardware.network.NetworkHardwareRepository.CidrAddress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContextNetwork {

    static String ping(String host, int port) throws ConnectException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception ignore) {
        }
        throw new ConnectException("Host %s:%s not reachable".formatted(host, port));
    }

    /**
     * Listen upd on host/port. default host is wildcard listener accept DatagramPacket and string value
     *
     * @param listener -
     * @param host     -
     * @param key      -
     * @param port     -
     */
    void listenUdp(String key, @Nullable String host, int port, BiConsumer<DatagramPacket, String> listener);

    void stopListenUdp(String key);

    @NotNull
    String getHostname();

    @NotNull
    String getOuterIpAddress();

    @NotNull
    List<ServiceInfo> scanMDNS(@NotNull String serviceType);

    void addNetworkAddressChanged(@NotNull String key, @NotNull BiConsumer<List<CidrAddress>, List<CidrAddress>> listener);

    void removeNetworkAddressChanged(@NotNull String key);

    default IpGeolocation getIpGeoLocation(String ip) {
        return fetchCached("http://ip-api.com/json/%s".formatted(ip), IpGeolocation.class);
    }

    default CityGeolocation getCityGeolocation(String city) {
        return fetchSingleNode("https://nominatim.openstreetmap.org/search?q=%s&format=json&addressdetails=1".formatted(city), CityGeolocation.class);
    }

    /*default CountryInfo getCountryInformation(String country) {
        return fetchSingleNode("https://restcountries.com/v3.1/name/%s".formatted(country), CountryInfo.class);
    }*/

    <T> T fetchCached(String address, Class<T> typeClass);

    @SneakyThrows
    private <T> T fetchSingleNode(String address, Class<T> resultType) {
        JsonNode jsonNode = fetchCached(address, JsonNode.class);
        if (jsonNode instanceof ArrayNode array) {
            jsonNode = array.iterator().next();
        }
        return OBJECT_MAPPER.readValue(jsonNode.toString(), resultType);
    }

    @Getter
    @Setter
    class IpGeolocation {

        private String query;
        private String status;
        private String country;
        private String countryCode;
        private String region;
        private String regionName;
        private String city;
        private String zip;
        private Double lat;
        private Double lon;
        private String timezone;
        private String isp;
        private String org;
        private String as;
    }

    @Getter
    @Setter
    class CityGeolocation {

        private Double lat;
        private Double lon;
    }
}
