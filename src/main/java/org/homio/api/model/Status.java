package org.homio.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public enum Status {
    ONLINE("#1F8D2D", "fas fa-check"),
    RUNNING("#B59324", "fas fa-person-running"),
    INITIALIZE("#CF79ED", "fas fa-spinner fa-spin"),
    WAITING("#506ABF", "fas fa-pause fa-fade"),
    OFFLINE("#969696", ""),
    UNKNOWN("#818744", ""),
    ERROR("#B22020", "fas fa-circle-exclamation"),
    REQUIRE_AUTH("#8C3581", "fas fa-triangle-exclamation"),
    NOT_SUPPORTED("#9C3E60", "fas fa-bug"),
    DONE("#399396", ""),
    NOT_READY("#99A040", "fas fa-book-skull"),
    CLOSING("#992F5D", "fas fa-door-closed fa-fade"),
    TESTING("#A3A18E", "fas fa-vial fa-beat"),
    RESTARTING("#99A040", "fas fa-hourglass-start fa-spin"),
    DISABLED("#9E9E9E", "fas fa-ban");

    @Getter
    private final String color;

    @Getter
    private final String icon;

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

    public StatusModel toModel() {
        return new StatusModel(this);
    }

    @Getter
    @RequiredArgsConstructor
    public static class StatusModel {

        private final @NotNull Status value;

        public String getColor() {
            return value.getColor();
        }

        public String getIcon() {
            return value.getIcon();
        }
    }
}
