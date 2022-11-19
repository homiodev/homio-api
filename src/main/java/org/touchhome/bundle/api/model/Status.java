package org.touchhome.bundle.api.model;

public enum Status {
    ONLINE, RUNNING, WAITING, OFFLINE, UNKNOWN, ERROR, REQUIRE_AUTH, NOT_SUPPORTED, DONE;

    public boolean isOnline() {
        return this == ONLINE;
    }

    public boolean isOffline() {
        return this != ONLINE && this != RUNNING && this != WAITING;
    }
}
