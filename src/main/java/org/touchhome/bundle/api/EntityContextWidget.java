package org.touchhome.bundle.api;

import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.entity.widget.ability.HasTimeValueSeries;

public interface EntityContextWidget {

    String LINE_CHART_WIDGET_PREFIX = "wgtlc_";

    EntityContext getEntityContext();

    /**
     * @param entityID -unique cline chart entity id. Must starts with
     *     EntityContextWidget.LINE_CHART_WIDGET_PREFIX
     * @param name widget name
     * @param chartBuilder - chart builder
     * @param lineChartWidgetBuilder - series builder
     * @param attachTabEntityID - tab attach to or null(will be attached to main tab)
     */
    void createLineChartWidget(
            @NotNull String entityID,
            @NotNull String name,
            @NotNull Consumer<LineChartSeriesBuilder> chartBuilder,
            @NotNull Consumer<LineChartWidgetBuilder> lineChartWidgetBuilder,
            @Nullable String attachTabEntityID);

    interface LineChartSeriesBuilder {
        void addLineChart(String color, HasTimeValueSeries lineChartSeries);
    }

    interface LineChartWidgetBuilder {

        LineChartWidgetBuilder showAxisX(boolean on);

        LineChartWidgetBuilder showAxisY(boolean on);

        LineChartWidgetBuilder axisLabelX(String name);

        LineChartWidgetBuilder axisLabelY(String name);
    }
}
