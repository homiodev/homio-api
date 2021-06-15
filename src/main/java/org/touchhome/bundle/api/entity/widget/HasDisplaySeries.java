package org.touchhome.bundle.api.entity.widget;

import org.touchhome.bundle.api.model.HasEntityIdentifier;

public interface HasDisplaySeries extends HasEntityIdentifier {
    Object getDisplayValue();
}
