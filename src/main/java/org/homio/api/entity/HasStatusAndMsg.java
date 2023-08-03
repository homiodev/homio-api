package org.homio.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.homio.api.EntityContextSetting;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.model.Status;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.color.UIFieldColorStatusMatch;
import org.homio.api.ui.field.condition.UIFieldShowOnCondition;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface HasStatusAndMsg<T extends HasEntityIdentifier> {

    String DISTINGUISH_KEY = "status";

    @UIField(order = 1, hideInEdit = true, hideOnEmpty = true, disableEdit = true)
    @UIFieldGroup(value = "STATUS", order = 3, borderColor = "#7ACC2D")
    @UIFieldColorStatusMatch
    @UIFieldShowOnCondition("return !context.get('compactMode')")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    default @NotNull Status getStatus() {
        return EntityContextSetting.getStatus(((T) this), DISTINGUISH_KEY, Status.UNKNOWN);
    }

    @UIField(order = 2, hideInEdit = true, hideOnEmpty = true, color = "#B22020")
    @UIFieldShowOnCondition("return !context.get('compactMode')")
    @UIFieldGroup(value = "STATUS", order = 3, borderColor = "#7ACC2D")
    default @Nullable String getStatusMessage() {
        return EntityContextSetting.getMessage(((T) this), DISTINGUISH_KEY);
    }

    default @NotNull T setStatusOnline() {
        return setStatus(Status.ONLINE, null);
    }

    default @NotNull T setStatusError(@NotNull Exception ex) {
        return setStatus(Status.ERROR, CommonUtils.getErrorMessage(ex));
    }

    default @NotNull T setStatusError(@NotNull String message) {
        return setStatus(Status.ERROR, message);
    }

    default @NotNull T setStatus(@NotNull Status status) {
        return setStatus(status, status == Status.ONLINE ? null : getStatusMessage());
    }

    default @NotNull T setStatus(@Nullable Status status, @Nullable String msg) {
        EntityContextSetting.setStatus((BaseEntityIdentifier) this, DISTINGUISH_KEY, "Status", status, msg);
        return (T) this;
    }

    default @NotNull T setStatusMessage(@Nullable String msg) {
        EntityContextSetting.setMessage(((T) this), DISTINGUISH_KEY, msg);
        return (T) this;
    }
}
