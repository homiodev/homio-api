package org.homio.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.homio.api.ContextSetting;
import org.homio.api.exception.ServerException;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.model.Status;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.UIFieldNoReadDefaultValue;
import org.homio.api.ui.field.UIFieldType;
import org.homio.api.ui.field.color.UIFieldColorStatusMatch;
import org.homio.api.ui.field.condition.UIFieldShowOnCondition;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;


public interface HasStatusAndMsg extends HasEntityIdentifier {

  String DISTINGUISH_KEY = "status";

  @UIField(order = 1, hideInEdit = true, hideOnEmpty = true, disableEdit = true)
  @UIFieldGroup(value = "STATUS", order = 3, borderColor = "#7ACC2D")
  @UIFieldColorStatusMatch
  @UIFieldShowOnCondition("return !context.get('compactMode')")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @UIFieldNoReadDefaultValue
  default @NotNull Status getStatus() {
    return ContextSetting.getStatus(this, DISTINGUISH_KEY, Status.UNKNOWN);
  }

  default void setStatus(@NotNull Status status) {
    setStatus(status, status == Status.ONLINE ? null : getStatusMessage());
  }

  @UIField(order = 6, hideInEdit = true, hideOnEmpty = true, color = "#B22020", type = UIFieldType.HTML)
  @UIFieldShowOnCondition("return !context.get('compactMode')")
  @UIFieldGroup(value = "STATUS", order = 3, borderColor = "#7ACC2D")
  @UIFieldNoReadDefaultValue
  default @Nullable String getStatusMessage() {
    return ContextSetting.getMessage(this, DISTINGUISH_KEY);
  }

  default void setStatusMessage(@Nullable String msg) {
    ContextSetting.setMessage(this, DISTINGUISH_KEY, msg);
  }

  default void setStatusOnline() {
    setStatus(Status.ONLINE, null);
  }

  default void setStatusError(@NotNull Exception ex) {
    if (ex instanceof ServerException se && se.getStatus() != null) {
      setStatus(se.getStatus(), CommonUtils.getErrorMessage(ex));
    } else {
      setStatus(Status.ERROR, CommonUtils.getErrorMessage(ex));
    }
  }

  default void setStatusError(@NotNull String message) {
    setStatus(Status.ERROR, message);
  }

  default void setStatus(@Nullable Status status, @Nullable String msg) {
    ContextSetting.setStatus(this, DISTINGUISH_KEY, "Status", status, msg);
  }

  default void addStatusUpdateListener(Consumer<Status> consumer) {

  }
}
