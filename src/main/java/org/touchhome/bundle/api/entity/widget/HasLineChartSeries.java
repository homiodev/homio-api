package org.touchhome.bundle.api.entity.widget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface HasLineChartSeries extends HasEntityIdentifier {
    /**
     * Return line chart series.
     * Usually getLineChartSeries should return only one chart, but sometimes it may be more than one)
     *
     * @param entityContext
     * @param parameters
     * @param from          start time
     * @param to            end time
     * @param dateFromNow   start time in string format i.e. 0 - from 1970, -1d - minus one day, etc...
     * @return LineChartDescription and list of points. point[0] - Date or long, point[1] - Float, point[2] - description. point[2] - optional
     */
    Map<LineChartDescription, List<Object[]>> getLineChartSeries(EntityContext entityContext, JSONObject parameters, Date from, Date to, String dateFromNow);

    static Query buildValuesQuery(EntityManager em, String queryName, BaseEntity baseEntity, Date from, Date to) {
        return em.createNamedQuery(queryName)
                .setParameter("source", baseEntity)
                .setParameter("from", from)
                .setParameter("to", to);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    class LineChartDescription {
        String name;
        String color;
    }
}
