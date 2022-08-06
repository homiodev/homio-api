package org.touchhome.bundle.api.entity.widget;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

import java.util.function.Consumer;

public interface HasSliderSeries extends HasEntityIdentifier {
    float getSliderValue(EntityContext entityContext, JSONObject dynamicParameters);

    void setSliderValue(float value, EntityContext entityContext, JSONObject dynamicParameters);

    void addUpdateValueListener(EntityContext entityContext, String key,
                                JSONObject dynamicParameters, Consumer<Object> listener);
}
