package org.touchhome.bundle.api.netty;

import org.jetbrains.annotations.NotNull;

/**
 * interface that allow specify component/service to use bootstrap server.
 * Allow to validate that all bootstrap servers will use different ports
 */
public interface HasBootstrapServer extends Comparable<HasBootstrapServer> {
    int getServerPort();

    String getName();

    @Override
    default int compareTo(@NotNull HasBootstrapServer o) {
        return Integer.compare(this.getServerPort(), o.getServerPort());
    }
}
