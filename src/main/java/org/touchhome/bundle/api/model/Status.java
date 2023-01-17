package org.touchhome.bundle.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Status {
    ONLINE("#1F8D2D"),
    RUNNING("#B59324"),
    INITIALIZE("#CF79ED"),
    WAITING("#506ABF"),
    OFFLINE("#969696"),
    UNKNOWN("#818744"),
    ERROR("#B22020"),
    REQUIRE_AUTH("#8C3581"),
    NOT_SUPPORTED("#9C3E60"),
    DONE("#399396"),
    NOT_READY("#99A040"),
    CLOSING("#992F5D"),
    RESTARTING("#99A040");

    @Getter private final String color;

    public boolean isOnline() {
        return this == ONLINE;
    }

    public boolean isOffline() {
        return this != ONLINE && this != RUNNING && this != WAITING;
    }

    public boolean inStatus(Status status, Status... extraStatusParams) {
        if (this == status) {
            return true;
        }
        for (Status extraStatus : extraStatusParams) {
            if (this == extraStatus) {
                return true;
            }
        }
        return false;
    }
}
