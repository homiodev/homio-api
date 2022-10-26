package org.touchhome.bundle.api.entity.widget.ability;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.entity.widget.ChartRequest;
import org.touchhome.bundle.api.exception.ProhibitedExecution;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation must override either {@link HasTimeValueSeries#getTimeValueSeries(ChartRequest)} or
 * {@link HasTimeValueSeries#getMultipleTimeValueSeries(ChartRequest)}
 */
public interface HasTimeValueSeries extends HasEntityIdentifier, HasUpdateValueListener {

    /**
     * Uses for UI to determine class type description
     */
    @JsonIgnore
    @SelectDataSourceDescription
    String getTimeValueSeriesDescription();

    /**
     * Return line chart series.
     * <p>
     * Usually getLineChartSeries should return only one chart, but sometimes it may be more than one)
     *
     * @return LineChartDescription and list of points. point[0] - Date or long, point[1] - Float, point[2] - description.
     * point[2] - optional
     */
    default @NotNull Map<TimeValueDatasetDescription, List<Object[]>> getMultipleTimeValueSeries(@NotNull ChartRequest request) {
        Object params = request.getParameters();
        int paramCode = (params == null ? "" : params).toString().hashCode();
        return Collections.singletonMap(new TimeValueDatasetDescription(getEntityID() + "_" +
                        paramCode),
                getTimeValueSeries(request));
    }

    default @NotNull List<Object[]> getTimeValueSeries(@NotNull ChartRequest request) {
        throw new ProhibitedExecution();
    }

    @Getter
    class TimeValueDatasetDescription {
        private final @NotNull String id;
        private final @Nullable String name;
        private @Nullable String color;

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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TimeValueDatasetDescription that = (TimeValueDatasetDescription) o;

            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}
