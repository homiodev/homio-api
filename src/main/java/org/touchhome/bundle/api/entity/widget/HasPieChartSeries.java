package org.touchhome.bundle.api.entity.widget;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

import java.util.Date;

public interface HasPieChartSeries extends HasEntityIdentifier {
    double getPieSumChartSeries(EntityContext entityContext, Date from, Date to, String dateFromNow);

    double getPieCountChartSeries(EntityContext entityContext, Date from, Date to, String dateFromNow);
}
