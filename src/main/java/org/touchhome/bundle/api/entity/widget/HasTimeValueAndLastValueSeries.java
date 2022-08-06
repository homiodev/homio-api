package org.touchhome.bundle.api.entity.widget;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

/**
 * Extend HasTimeValueSeries and add getLastAvailableValue() method. Mostly for Mini-card widgets
 */
public interface HasTimeValueAndLastValueSeries extends HasTimeValueSeries {
    @Nullable
    Object getLastAvailableValue(@Nullable JSONObject parameters);
}
