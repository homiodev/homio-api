package org.homio.api.model;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public enum Status implements OptionModel.HasIcon {
  ONLINE("#6E993D", "fas fa-check"),
  RUNNING("#B59324", "fas fa-person-running"),
  INITIALIZE("#CF79ED", "fas fa-spinner fa-spin"),
  UPDATING("#602183", "fas fa-compact-disc fa-spin"),
  RESTARTING("#99A040", "fas fa-hourglass-start fa-spin"),
  WAITING("#506ABF", "fas fa-pause fa-fade"),
  CLOSING("#992F5D", "fas fa-door-closed fa-fade"),
  TESTING("#A3A18E", "fas fa-vial fa-beat"),
  DONE("#399396", "fas fa-forward"),
  UNKNOWN("#818744", "fas fa-circle-question"),
  NOT_SUPPORTED("#9C3E60", "fas fa-bug"),
  NOT_READY("#99A040", "fas fa-triangle-exclamation"),
  REQUIRE_AUTH("#8C3581", "fas fa-triangle-exclamation"),
  ERROR("#B22020", "fas fa-circle-exclamation"),
  OFFLINE("#969696", "fab fa-hashnode"),
  DISABLED("#9E9E9E", "fas fa-ban"),
  SLEEPING("#33474F", "fas fa-bed");

  private final String color;

  @Getter private final String icon;

  public static Set<String> set(Status... statuses) {
    return Arrays.stream(statuses).map(Enum::name).collect(Collectors.toSet());
  }

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

  public EntityStatus toModel() {
    return new EntityStatus(this);
  }

  @Getter
  @RequiredArgsConstructor
  public static class EntityStatus {

    private final @NotNull Status value;

    public String getColor() {
      return value.getColor();
    }

    public String getIcon() {
      return value.getIcon();
    }
  }
}
