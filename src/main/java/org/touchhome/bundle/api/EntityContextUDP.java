package org.touchhome.bundle.api;

import java.net.DatagramPacket;
import java.util.function.BiConsumer;
import org.springframework.lang.Nullable;

public interface EntityContextUDP {
    /**
     * Listen upd on host/port. default host is wildcard listener accept DatagramPacket and string
     * value
     */
    void listenUdp(
            String key,
            @Nullable String host,
            int port,
            BiConsumer<DatagramPacket, String> listener);

    void stopListenUdp(String key);
}
