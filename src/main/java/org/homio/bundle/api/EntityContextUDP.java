package org.homio.bundle.api;

import java.net.DatagramPacket;
import java.util.function.BiConsumer;
import org.springframework.lang.Nullable;

public interface EntityContextUDP {
    /**
     * Listen upd on host/port. default host is wildcard
     * listener accept DatagramPacket and string value
     * @param listener -
     * @param host -
     * @param key -
     * @param port -
     */
    void listenUdp(String key, @Nullable String host, int port, BiConsumer<DatagramPacket, String> listener);

    void stopListenUdp(String key);
}
