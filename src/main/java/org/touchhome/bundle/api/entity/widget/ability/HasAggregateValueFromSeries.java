package org.touchhome.bundle.api.entity.widget.ability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.entity.widget.AggregationType;
import org.touchhome.bundle.api.entity.widget.ChartRequest;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

public interface HasAggregateValueFromSeries extends HasEntityIdentifier, HasUpdateValueListener {

    /**
     * Aggregate value or take first/last values.
     *
     * @param request
     * @param aggregationType
     * @param exactNumber     - if aggregate exact number values or take any value (i.e.: for display widget)
     * @return
     */
    @Nullable Object getAggregateValueFromSeries(@NotNull ChartRequest request,
                                                 @NotNull AggregationType aggregationType,
                                                 boolean exactNumber);

    default AggregationType[] getAvailableAggregateTypes() {
        return AggregationType.values();
    }

    /**
     * Uses for UI to determine class type description
     */
    @SelectDataSourceDescription
    String getAggregateValueDescription();
}
