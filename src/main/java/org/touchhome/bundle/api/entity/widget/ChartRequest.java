package org.touchhome.bundle.api.entity.widget;

import java.util.Date;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;

@Getter
public class ChartRequest {
    @NotNull private final EntityContext entityContext;
    @Nullable private final Date from;
    @Nullable private final Date to;

    private JSONObject parameters = new JSONObject();

    public ChartRequest(
            @NotNull EntityContext entityContext, @Nullable Date from, @Nullable Date to) {
        this.entityContext = entityContext;
        this.from = from;
        this.to = to;
    }

    public ChartRequest setParameters(JSONObject parameters) {
        if (parameters != null) {
            this.parameters = parameters;
        }
        return this;
    }

    public boolean isBetween(long timestamp) {
        return (from == null || timestamp > from.getTime())
                && (to == null || timestamp < to.getTime());
    }

    public Long getFromTime() {
        return from == null ? null : from.getTime();
    }

    public Long getToTime() {
        return to == null ? null : to.getTime();
    }
}
