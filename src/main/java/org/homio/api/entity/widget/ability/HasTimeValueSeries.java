package org.homio.api.entity.widget.ability;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import org.homio.api.entity.widget.PeriodRequest;
import org.homio.api.exception.ProhibitedExecution;
import org.homio.api.model.HasEntityIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation must override either {@link HasTimeValueSeries#getTimeValueSeries(PeriodRequest)} or
 * {@link HasTimeValueSeries#getMultipleTimeValueSeries(PeriodRequest)}
 */
public interface HasTimeValueSeries extends HasEntityIdentifier, HasUpdateValueListener,
    // we extend HasGetStatusValue for time-series values to be able to fetch last value in case
    // if no data found in time range, but we need fill chart with empty values
    HasGetStatusValue {

    /**
     * @return Uses for UI to determine class type description
     */
    @JsonIgnore
    @SelectDataSourceDescription
    String getTimeValueSeriesDescription();

    /**
     * Return line chart series.
     * <p>
     * Usually getLineChartSeries should return only one chart, but sometimes it may be more than one)
     *
     * @param request -
     * @return LineChartDescription and list of points. point[0] - Date or long, point[1] - Float, point[2] - description. point[2] - optional
     */
    default @NotNull Map<TimeValueDatasetDescription, List<Object[]>> getMultipleTimeValueSeries(@NotNull PeriodRequest request) {
        Object params = request.getParameters();
        int paramCode = (params == null ? "" : params).toString().hashCode();
        return new HashMap<>(Map.of(new TimeValueDatasetDescription(getEntityID() + "_" + paramCode),
            getTimeValueSeries(request)));
    }

    default @NotNull List<Object[]> getTimeValueSeries(@NotNull PeriodRequest request) {
        throw new ProhibitedExecution();
    }

    @Getter
    class TimeValueDatasetDescription {

        private final @NotNull String id;
        private final @Nullable String name;
        private final @Nullable String color;

        public TimeValueDatasetDescription(@NotNull String id) {
            this(id, null, null);
        }

        public TimeValueDatasetDescription(@NotNull String id, @Nullable String name) {
            this(id, name, null);
        }

        public TimeValueDatasetDescription(@NotNull String id, @Nullable String name, @Nullable String color) {
            this.id = id;
            this.name = name;
            this.color = color;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {return true;}
            if (o == null || getClass() != o.getClass()) {return false;}

            TimeValueDatasetDescription that = (TimeValueDatasetDescription) o;

            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}
