package org.homio.api.entity.widget.ability;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.homio.api.entity.widget.AggregationType;
import org.homio.api.entity.widget.PeriodRequest;
import org.homio.api.model.HasEntityIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HasAggregateValueFromSeries extends HasEntityIdentifier, HasUpdateValueListener {

    /**
     * Aggregate value or take first/last values.
     *
     * @param request         -
     * @param aggregationType -
     * @param exactNumber     - if aggregate exact number values or take any value (i.e.: for display widget)
     * @return -
     */
    @Nullable Object getAggregateValueFromSeries(@NotNull PeriodRequest request,
                                                 @NotNull AggregationType aggregationType,
                                                 boolean exactNumber);

    /**
     * @return Uses for UI to determine class type description
     */
    @JsonIgnore
    @SelectDataSourceDescription
    String getAggregateValueDescription();
}
