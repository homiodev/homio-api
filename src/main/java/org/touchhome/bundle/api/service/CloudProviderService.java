package org.touchhome.bundle.api.service;

import org.touchhome.bundle.api.model.Status;

public interface CloudProviderService {
    String getName();

    Status getStatus();

    String getStatusMessage();

    void start();

    void stop();
}
