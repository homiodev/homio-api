package org.touchhome.bundle.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.entity.widget.HasLineChartSeries;
import org.touchhome.bundle.api.entity.widget.WidgetTabEntity;

import java.util.function.Consumer;

public interface EntityContextWidget {

    String LINE_CHART_WIDGET_PREFIX = "lcw_";

    EntityContext getEntityContext();

    /**
     * @param entityID               -unique cline chart entity id. Must starts with EntityContextWidget.LINE_CHART_WIDGET_PREFIX
     * @param name                   widget name
     * @param chartBuilder           - chart builder
     * @param lineChartWidgetBuilder series builder
     * @param attachTab              - tabb attach to or null(will be attached to main tab)
     */
    void createLineChartWidget(@NotNull String entityID, @NotNull String name, @NotNull Consumer<LineChartSeriesBuilder> chartBuilder,
                               @NotNull Consumer<LineChartWidgetBuilder> lineChartWidgetBuilder, @Nullable WidgetTabEntity attachTab);

    interface LineChartSeriesBuilder {
        void addLineChart(String color, HasLineChartSeries lineChartSeries);
    }

    interface LineChartWidgetBuilder {

        LineChartWidgetBuilder showButtons(boolean on);

        LineChartWidgetBuilder showAxisX(boolean on);

        LineChartWidgetBuilder showAxisY(boolean on);

        LineChartWidgetBuilder axisLabelX(String name);

        LineChartWidgetBuilder axisLabelY(String name);

        LineChartWidgetBuilder timeline(String value);
    }
}
