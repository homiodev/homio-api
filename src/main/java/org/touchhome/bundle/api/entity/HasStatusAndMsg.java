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
import org.touchhome.common.util.CommonUtils;

import java.util.Objects;
import java.util.Optional;

public interface HasStatusAndMsg<T extends HasEntityIdentifier> {

    Pair<Status, String> DEFAULT_STATUS = Pair.of(Status.UNKNOWN, "");

    @UIField(order = 10, readOnly = true, hideOnEmpty = true)
    @UIFieldColorStatusMatch
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    default Status getStatus() {
        String entityID = ((T) this).getEntityID();
        return entityID == null ? Status.UNKNOWN : TouchHomeUtils.STATUS_MAP.getOrDefault(entityID, DEFAULT_STATUS).getFirst();
    }

    @UIField(order = 11, readOnly = true, hideOnEmpty = true)
    @UIFieldColorStatusMatch
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    default Status getJoined() {
        String entityID = ((T) this).getEntityID();
        return Optional.ofNullable(TouchHomeUtils.STATUS_MAP.get(entityID + "_joined")).map(Pair::getFirst).orElse(null);
    }

    default T setStatusOnline() {
        return setStatus(Status.ONLINE, null);
    }

    default T setStatusError(@NotNull Exception ex) {
        return setStatus(Status.ERROR, CommonUtils.getErrorMessage(ex));
    }

    default T setStatusError(@NotNull String message) {
        return setStatus(Status.ERROR, message);
    }

    default T setStatus(@NotNull Status status) {
        return setStatus(status, status == Status.ONLINE ? null : getStatusMessage());
    }

    default T setJoined(@NotNull Status status) {
        TouchHomeUtils.STATUS_MAP.put(((T) this).getEntityID() + "_joined", Pair.of(status, ""));
        return (T) this;
    }

    default T setStatus(@Nullable Status status, @Nullable String msg) {
        if (status != null) {
            String entityID = ((T) this).getEntityID();
            Pair<Status, String> prev = TouchHomeUtils.STATUS_MAP.get(entityID);
            if (prev != null && prev.getFirst() != status && !Objects.equals(prev.getSecond(), msg)) {
                logChangeStatus(status, msg);
            }
            TouchHomeUtils.STATUS_MAP.put(entityID, Pair.of(status, msg == null ? "" : msg));
        }
        return (T) this;
    }

    @UIField(order = 23, readOnly = true, hideOnEmpty = true)
    default String getStatusMessage() {
        String entityID = ((T) this).getEntityID();
        return entityID == null ? null : TouchHomeUtils.STATUS_MAP.getOrDefault(entityID, DEFAULT_STATUS).getSecond();
    }

    default T setStatusMessage(@Nullable String msg) {
        return setStatus(getStatus(), msg);
    }

    /**
     * Override in entity if need log change status
     */
    default void logChangeStatus(Status status, String message) {
        // do nothing
    }
}
