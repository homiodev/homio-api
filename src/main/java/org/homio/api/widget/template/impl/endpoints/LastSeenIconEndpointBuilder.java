package org.homio.api.widget.template.impl.endpoints;

import org.homio.api.EntityContextWidget.HasIcon;
import org.homio.api.EntityContextWidget.ValueCompare;
import org.homio.api.ui.UI.Color;

public class LastSeenIconEndpointBuilder implements IconEndpointBuilder {

    @Override
    public void build(HasIcon<?> iconWidgetBuilder) {
        iconWidgetBuilder.setIconColor("#009688", colorBuilder ->
                colorBuilder.setThreshold(Color.WARNING, 30, ValueCompare.gt)
                        .setThreshold(Color.RED, 120, ValueCompare.gt));
    }
}
