package org.homio.api.widget.template.impl.endpoints;

import org.homio.api.EntityContextWidget.HasIcon;
import org.homio.api.EntityContextWidget.ValueCompare;
import org.homio.api.ui.UI.Color;

public class BatteryIconEndpointBuilder implements IconEndpointBuilder {

    @Override
    public void build(HasIcon<?> iconWidgetBuilder) {
        iconWidgetBuilder.setIcon("fas fa-battery-full", thresholdBuilder ->
            thresholdBuilder.setThreshold("fas fa-battery-three-quarters", 75, ValueCompare.lt)
                            .setThreshold("fas fa-battery-half", 50, ValueCompare.lt)
                            .setThreshold("fas fa-battery-quarter", 25, ValueCompare.lt)
                            .setThreshold("fas fa-battery-empty", 10, ValueCompare.lt));
        iconWidgetBuilder.setIconColor(Color.BLUE, colorBuilder ->
            colorBuilder.setThreshold(Color.WARNING, 15, ValueCompare.lt)
                        .setThreshold(Color.RED, 10, ValueCompare.lt));
    }
}
