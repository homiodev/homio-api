package org.homio.api.widget.template.impl.endpoints;

import org.homio.api.ContextWidget.HasIcon;
import org.homio.api.ContextWidget.ValueCompare;
import org.homio.api.ui.UI.Color;

public class HumidityIconEndpointBuilder implements IconEndpointBuilder {

  @Override
  public void build(HasIcon<?> iconWidgetBuilder) {
    iconWidgetBuilder.setIcon("fas fa-droplet", iconBuilder -> {
      iconBuilder.setThreshold("fas fa-cloud-rain", 55, ValueCompare.gt)
        .setThreshold("fas fa-fire", 30, ValueCompare.lt);
      iconWidgetBuilder.setIconColor(Color.GREEN, colorBuilder -> {
        colorBuilder.setThreshold(Color.WARNING, 30, ValueCompare.lt);
        colorBuilder.setThreshold(Color.RED, 55, ValueCompare.gt);
      });
    });
  }
}
