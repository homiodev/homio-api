package org.touchhome.bundle.api.entity.widget;

import org.touchhome.bundle.api.model.HasEntityIdentifier;

public interface HasSliderSeries extends HasEntityIdentifier {
    float getSliderValue();

    void setSliderValue(float value);
}
