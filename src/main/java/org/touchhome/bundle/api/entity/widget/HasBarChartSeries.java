package org.touchhome.bundle.api.entity.widget;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

public interface HasBarChartSeries extends HasEntityIdentifier {
    double getBarValue(EntityContext entityContext);
}
