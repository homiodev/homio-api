package org.touchhome.bundle.api.entity.widget;

import org.touchhome.bundle.api.model.HasEntityIdentifier;

public interface HasToggleSeries extends HasEntityIdentifier {
    Boolean getToggleValue();

    void setToggleValue(boolean value);
}
