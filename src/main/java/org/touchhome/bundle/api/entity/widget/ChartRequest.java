package org.touchhome.bundle.api.entity.widget;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;

import java.util.Date;

@Getter
public class ChartRequest {
    @NotNull
    private final EntityContext entityContext;
    @NotNull
    private final Date from;
    @NotNull
    private final Date to;
    // start time in string format i.e. 0 - from 1970, -1d - minus one day, etc...
    private final String dateFromNow;

    private final boolean requireFilterByDate;

    private JSONObject parameters = new JSONObject();

    public ChartRequest(EntityContext entityContext, Date from, Date to, String dateFromNow, boolean requireFilterByDate) {
        this.entityContext = entityContext;
        this.from = from == null ? new Date(0) : from;
        this.to = to == null ? new Date() : to;
        this.dateFromNow = dateFromNow;
        this.requireFilterByDate = requireFilterByDate;
    }

    public ChartRequest setParameters(JSONObject parameters) {
        this.parameters = parameters;
        return this;
    }

    public boolean isBetween(long timestamp) {
        return timestamp > from.getTime() && timestamp < to.getTime();
    }
}
