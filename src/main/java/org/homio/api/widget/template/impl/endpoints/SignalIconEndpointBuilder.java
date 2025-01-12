package org.homio.api.widget.template.impl.endpoints;

import org.homio.api.ContextWidget.HasIcon;
import org.homio.api.ContextWidget.ValueCompare;
import org.homio.api.ui.UI.Color;

public class SignalIconEndpointBuilder implements IconEndpointBuilder {

  @Override
  public void build(HasIcon<?> iconWidgetBuilder) {
    iconWidgetBuilder.setIconColor(Color.GREEN, colorBuilder ->
      colorBuilder.setThreshold(Color.WARNING, 50, ValueCompare.lt)
        .setThreshold(Color.RED, 0, ValueCompare.eq));
  }
}
