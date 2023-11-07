package org.homio.api;

import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.function.BiConsumer;
import javax.jmdns.ServiceInfo;
import org.homio.hquery.hardware.network.NetworkHardwareRepository.CidrAddress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContextNetwork {

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

    static String ping(String host, int port) throws ConnectException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("google.com", port), 5000);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception ignore) {
        }
        throw new ConnectException("Host %s:%s not reachable".formatted(host, port));
    }

    @NotNull String getHostname();

    @NotNull List<ServiceInfo> scanMDNS(@NotNull String serviceType);

    void addNetworkAddressChanged(@NotNull String key, @NotNull BiConsumer<List<CidrAddress>, List<CidrAddress>> listener);

    void removeNetworkAddressChanged(@NotNull String key);
}
