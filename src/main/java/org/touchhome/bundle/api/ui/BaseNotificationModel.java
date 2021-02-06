package org.touchhome.bundle.api.ui;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Getter
@Accessors(chain = true)
public class BaseNotificationModel<T extends BaseNotificationModel> implements Comparable<T> {
    private final String entityID;

    @Setter
    private String title;

    @Setter
    private Object value;

    private Date creationTime = new Date();

    public BaseNotificationModel(String entityID) {
        if (entityID == null) {
            throw new IllegalArgumentException("entityId is null");
        }
        this.entityID = entityID;
        this.title = entityID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        T that = (T) o;
        return Objects.equals(entityID, that.getEntityID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityID);
    }

    @Override
    public int compareTo(@NotNull T other) {
        return StringUtils.defaultString(this.title, this.entityID).compareTo(StringUtils.defaultString(other.getTitle(), other.getEntityID()));
    }

    @Override
    public String toString() {
        return defaultString(title, "") + (value != null ? " | " + value : "");
    }
}
