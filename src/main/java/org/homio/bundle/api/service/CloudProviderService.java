package org.homio.bundle.api.service;

import org.homio.bundle.api.model.Status;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CloudProviderService {

    @NotNull String getName();

    @Nullable
    Status getStatus();

    @Nullable String getStatusMessage();

    void start() throws Exception;

    void stop() throws Exception;

    void updateNotificationBlock(@Nullable Exception ex);

    default void updateNotificationBlock() {
        updateNotificationBlock(null);
    }
}
