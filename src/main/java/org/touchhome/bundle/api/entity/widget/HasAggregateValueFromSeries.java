package org.touchhome.bundle.api.entity.widget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

import java.util.function.Consumer;

public interface HasAggregateValueFromSeries extends HasEntityIdentifier {

    void addUpdateValueListener(EntityContext entityContext, String key,
                                JSONObject dynamicParameters, Consumer<Object> listener);
    @Nullable Float getAggregateValueFromSeries(@NotNull ChartRequest request, @NotNull AggregationType aggregationType);
}
