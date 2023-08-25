package org.homio.api.entity.widget;

import java.util.Date;
import lombok.Getter;
import org.homio.api.EntityContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

@Getter
public class PeriodRequest {

    @NotNull
    private final EntityContext entityContext;
    @Nullable
    private final Date from;
    @Nullable
    private final Date to;

    private JSONObject parameters = new JSONObject();

    public PeriodRequest(@NotNull EntityContext entityContext, @Nullable Date from, @Nullable Date to) {
        this.entityContext = entityContext;
        this.from = from;
        this.to = to;
    }

    public PeriodRequest(@NotNull EntityContext entityContext, @Nullable Long diffMilliseconds) {
        this.entityContext = entityContext;
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
