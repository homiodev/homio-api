package org.homio.api.entity.widget;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.homio.api.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Date;

@Getter
public class PeriodRequest {

  @NotNull
  private final @Accessors(fluent = true) Context context;
  @Nullable
  private final Date from;
  @Nullable
  private final Date to;

  private @Setter int minItemsCount = -1;
  private @Setter boolean forward = true;
  private @Setter boolean sortAsc = true;

  private JSONObject parameters = new JSONObject();

  public PeriodRequest(@NotNull Context context, @Nullable Date from, @Nullable Date to) {
    this.context = context;
    this.from = from;
    this.to = to;
  }

  public PeriodRequest(@NotNull Context context, @Nullable Long diffMilliseconds) {
    this.context = context;
    this.from = diffMilliseconds == null ? null : new Date(System.currentTimeMillis() - diffMilliseconds);
    this.to = null;
  }

  public PeriodRequest setParameters(JSONObject parameters) {
    if (parameters != null) {
      this.parameters = parameters;
    }
    return this;
  }

  public boolean isBetween(long timestamp) {
    return (from == null || timestamp > from.getTime()) && (to == null || timestamp < to.getTime());
  }

  public Long getFromTime() {
    return from == null ? null : from.getTime();
  }

  public Long getToTime() {
    return to == null ? null : to.getTime();
  }
}
