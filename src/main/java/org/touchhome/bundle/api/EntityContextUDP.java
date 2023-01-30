package org.touchhome.bundle.api;

import org.springframework.lang.Nullable;

import java.net.DatagramPacket;
import java.util.function.BiConsumer;

public interface EntityContextUDP {
    /**
     * Listen upd on host/port. default host is wildcard
     * listener accept DatagramPacket and string value
     */
    void listenUdp(String key, @Nullable String host, int port, BiConsumer<DatagramPacket, String> listener);

    void stopListenUdp(String key);
}
