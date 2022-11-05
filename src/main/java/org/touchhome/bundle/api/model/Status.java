package org.touchhome.bundle.api.model;

public enum Status {
    OFFLINE, ONLINE, UNKNOWN, ERROR, RUNNING, WAITING, REQUIRE_AUTH, NOT_SUPPORTED, DONE;

    public boolean isOnline() {
        return this == Status.ONLINE;
    }
}
