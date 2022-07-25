package org.touchhome.bundle.api.entity.widget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

public interface HasAggregateValueFromSeries extends HasEntityIdentifier {
    @Nullable Float getAggregateValueFromSeries(@NotNull ChartRequest request, @NotNull AggregationType aggregationType);
}
