package org.touchhome.bundle.api.entity;

import org.touchhome.bundle.api.model.Status;

public interface HasStatusAndMsg<T> {

    Status getStatus();

    T setStatus(Status status);

    String getStatusMessage();

    T setStatusMessage(String msg);
}
