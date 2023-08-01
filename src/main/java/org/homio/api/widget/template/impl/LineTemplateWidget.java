package org.homio.api.widget.template.impl;

import static org.homio.api.widget.template.impl.DisplayTemplateWidget.fillHasLineChartBehaviour;

import java.util.List;
import org.homio.api.EntityContext;
import org.homio.api.entity.DeviceEndpointsBaseEntity;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.widget.template.TemplateWidgetBuilder;
import org.homio.api.widget.template.WidgetDefinition;
import org.homio.api.widget.template.WidgetDefinition.ItemDefinition;
import org.homio.api.widget.template.WidgetDefinition.Options.Chart;

public class LineTemplateWidget implements TemplateWidgetBuilder {

    @Override
    public void buildWidget(WidgetRequest widgetRequest) {
        WidgetDefinition widgetDefinition = widgetRequest.getWidgetDefinition();

        var request = new MainWidgetRequest(widgetRequest, widgetDefinition, 0,
            0, builder ->
            TemplateWidgetBuilder.buildCommon(widgetDefinition, widgetRequest, builder));
        buildMainWidget(request);
    }

    @Override
    public void buildMainWidget(MainWidgetRequest request) {
        EntityContext entityContext = request.getWidgetRequest().getEntityContext();
        DeviceEndpointsBaseEntity entity = request.getWidgetRequest().getEntity();

        WidgetDefinition wd = request.getItem();
        List<DeviceEndpoint> barSeries = wd.getIncludeEndpoints(request);

        entityContext.widget().createLineChartWidget("ln-" + entity.getIeeeAddress(), builder -> {
            TemplateWidgetBuilder.buildCommon(wd, request.getWidgetRequest(), builder);
            builder.setBlockSize(wd.getBlockWidth(3), wd.getBlockHeight(1))
                   .setShowAxisX(wd.getOptions().isShowAxisX())
                   .setShowAxisY(wd.getOptions().isShowAxisY())
                   .setShowChartFullScreenButton(wd.getOptions().isShowChartFullScreenButton())
                   .setChartPointsPerHour(wd.getOptions().getPointsPerHour())
                   .setPointRadius(wd.getOptions().getPointRadius())
                   .setShowDynamicLine(wd.getOptions().isShowDynamicLine())
                   .setDynamicLineColor(wd.getOptions().getDynamicLineColor())
                   .setPointBorderColor(wd.getOptions().getPointBorderColor());
            request.getAttachToLayoutHandler().accept(builder);

            for (DeviceEndpoint series : barSeries) {
                builder.addSeries(series.getName(false), seriesBuilder -> {
                    seriesBuilder.setChartDataSource(TemplateWidgetBuilder.getSource(entityContext, series, false));
                    ItemDefinition itemDefinition = wd.getEndpoint(series.getEndpointEntityID());
                    if (itemDefinition != null) {
                        Chart chart = itemDefinition.getChart();
                        String color = series.getIcon().getColor();
                        if (chart != null) {
                            fillHasLineChartBehaviour(builder, chart);
                            color = chart.getColor();
                        }
                        seriesBuilder.setChartColor(color);
                    }
                });
            }
        });
    }

    @Override
    public int getWidgetHeight(MainWidgetRequest request) {
        return request.getItem().getWidgetHeight(1);
    }
}
