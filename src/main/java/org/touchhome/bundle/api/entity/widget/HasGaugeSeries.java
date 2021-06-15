package org.touchhome.bundle.api.entity.widget;

import org.touchhome.bundle.api.model.HasEntityIdentifier;

public interface HasGaugeSeries extends HasEntityIdentifier {
    float getGaugeValue();
}
