package org.touchhome.bundle.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.EntityContextSetting;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.color.UIFieldColorStatusMatch;
import org.touchhome.bundle.api.util.TouchHomeUtils;


public interface HasStatusAndMsg<T extends HasEntityIdentifier> {

    String DISTINGUISH_KEY = "status";

    @UIField(order = 10, hideInEdit = true, hideOnEmpty = true)
    @UIFieldColorStatusMatch
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    default Status getStatus() {
        return EntityContextSetting.getStatus(((T) this), DISTINGUISH_KEY, Status.UNKNOWN);
    }

    @UIField(order = 11, hideInEdit = true, hideOnEmpty = true)
    default String getStatusMessage() {
        return EntityContextSetting.getMessage(((T) this), DISTINGUISH_KEY);
    }

    default T setStatusOnline() {
        return setStatus(Status.ONLINE, null);
    }

    default T setStatusError(@NotNull Exception ex) {
        return setStatus(Status.ERROR, TouchHomeUtils.getErrorMessage(ex));
    }

    default T setStatusError(@NotNull String message) {
        return setStatus(Status.ERROR, message);
    }

    default T setStatus(@NotNull Status status) {
        return setStatus(status, status == Status.ONLINE ? null : getStatusMessage());
    }

    default T setStatus(@Nullable Status status, @Nullable String msg) {
        EntityContextSetting.setStatus((BaseEntityIdentifier) this, DISTINGUISH_KEY, "Status", status, msg);
        return (T) this;
    }

    default T setStatusMessage(@Nullable String msg) {
        EntityContextSetting.setMessage(((T) this), DISTINGUISH_KEY, msg);
        return (T) this;
    }
}
