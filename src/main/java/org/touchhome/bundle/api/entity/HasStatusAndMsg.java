package org.touchhome.bundle.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.util.Pair;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.color.UIFieldColorStatusMatch;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public interface HasStatusAndMsg<T extends HasEntityIdentifier> {

    Pair<Status, String> DEFAULT_STATUS = Pair.of(Status.UNKNOWN, "");

    @UIField(order = 22, readOnly = true)
    @UIFieldColorStatusMatch
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    default Status getStatus() {
        String entityID = ((T) this).getEntityID();
        return entityID == null ? Status.UNKNOWN : TouchHomeUtils.STATUS_MAP.getOrDefault(((T) this).getEntityID(), DEFAULT_STATUS).getFirst();
    }

    default T setStatusOnline() {
        return setStatus(Status.ONLINE, null);
    }


    default T setStatusError(@NotNull Exception ex) {
        return setStatus(Status.ERROR, TouchHomeUtils.getErrorMessage(ex));
    }

    default T setStatus(@NotNull Status status) {
        return setStatus(status, status == Status.ONLINE ? null : getStatusMessage());
    }

    default T setStatus(@NotNull Status status, @Nullable String msg) {
        TouchHomeUtils.STATUS_MAP.put(((T) this).getEntityID(), Pair.of(status, msg == null ? "" : msg));
        return (T) this;
    }

    @UIField(order = 23, readOnly = true, hideOnEmpty = true)
    @Column(length = 512)
    default String getStatusMessage() {
        String entityID = ((T) this).getEntityID();
        return entityID == null ? null : TouchHomeUtils.STATUS_MAP.getOrDefault(entityID, DEFAULT_STATUS).getSecond();
    }

    default T setStatusMessage(@Nullable String msg) {
        return setStatus(getStatus(), msg);
    }
}
