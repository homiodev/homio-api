package org.touchhome.bundle.api.netty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class NettyUtils {

    private static Map<String, HasBootstrapServer> bootstrapServerMap = new HashMap<>();

    public static <T extends HasBootstrapServer> T removeBootstrapServer(String key) {
        return (T) bootstrapServerMap.remove(key);
    }

    public static synchronized <T extends HasBootstrapServer> T putBootstrapServer(String key, Supplier<T> hasBootstrapServer) {
        HasBootstrapServer bootstrapServer = bootstrapServerMap.get(key);
        if (bootstrapServer == null) {
            bootstrapServer = hasBootstrapServer.get();
            putBootstrapServer(key, bootstrapServer);
        }
        return (T) bootstrapServer;
    }

    public static synchronized <T extends HasBootstrapServer> void putBootstrapServer(String key, T hasBootstrapServer) {
        HasBootstrapServer server = getServerByPort(key, hasBootstrapServer.getServerPort());
        if (server != null) {
            throw new RuntimeException("Another Bootstrap server: " + server.getName() + " already use port: " +
                    hasBootstrapServer.getServerPort());
        }
        bootstrapServerMap.put(key, hasBootstrapServer);
    }

    public static int findFreeBootstrapServerPort() {
        AtomicInteger freePort = new AtomicInteger(9200);
        while (bootstrapServerMap.values().stream().anyMatch(h -> h.getServerPort() == freePort.get())) {
            freePort.incrementAndGet();
        }
        return freePort.get();
    }

    public static HasBootstrapServer getServerByPort(String excludeKey, int port) {
        return bootstrapServerMap.entrySet().stream()
                .filter(e -> !e.getKey().equals(excludeKey) && e.getValue().getServerPort() == port)
                .findAny().map(e -> e.getValue()).orElse(null);
    }
}
