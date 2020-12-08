package org.touchhome.bundle.api.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.util.NotificationLevel;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Getter
@Accessors(chain = true)
public class NotificationModel implements Comparable<NotificationModel> {

    private final String entityID;

    @Setter
    private String title;

    @Setter
    private Object value;

    @Setter
    private NotificationLevel level = NotificationLevel.info;

    private Date creationTime = new Date();

    public NotificationModel(String entityID) {
        if (entityID == null) {
            throw new IllegalArgumentException("entityId is null");
        }
        this.entityID = entityID;
        this.title = entityID;
    }

    public static NotificationModel danger(String entityID) {
        return new NotificationModel(entityID).setLevel(NotificationLevel.error);
    }

    public static NotificationModel warn(String entityID) {
        return new NotificationModel(entityID).setLevel(NotificationLevel.warning);
    }

    public static NotificationModel info(String entityID) {
        return new NotificationModel(entityID).setLevel(NotificationLevel.info);
    }

    public static NotificationModel success(String entityID) {
        return new NotificationModel(entityID).setLevel(NotificationLevel.success);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationModel that = (NotificationModel) o;
        return Objects.equals(entityID, that.entityID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityID);
    }

    @Override
    public int compareTo(@NotNull NotificationModel other) {
        int i = this.level.name().compareTo(other.level.name());
        return i == 0 ? this.title.compareTo(other.title) : i;
    }

    @Override
    public String toString() {
        return defaultString(title, "") + (value != null ? " | " + value : "");
    }
}
