package org.homio.api.widget.template.impl;

import java.util.List;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.widget.template.TemplateWidgetBuilder;
import org.homio.api.widget.template.WidgetDefinition;

public class BarTimeTemplateWidget implements TemplateWidgetBuilder {

    @Override
    public void buildWidget(WidgetRequest widgetRequest) {
        WidgetDefinition wd = widgetRequest.getWidgetDefinition();

        var request = new MainWidgetRequest(widgetRequest, wd, 0,
            0, builder -> TemplateWidgetBuilder.buildCommon(wd, widgetRequest, builder));
        buildMainWidget(request);
    }

    @Override
    public void buildMainWidget(MainWidgetRequest request) {
        WidgetRequest widgetRequest = request.getWidgetRequest();
        DeviceEndpointsBehaviourContract entity = widgetRequest.getEntity();
        WidgetDefinition wd = request.getItem();

        List<DeviceEndpoint> barSeries = wd.getIncludeEndpoints(request);
        widgetRequest.getEntityContext().widget().createBarTimeChartWidget("bt-" + entity.getIeeeAddress(), builder -> {
            TemplateWidgetBuilder.buildCommon(wd, widgetRequest, builder);
            builder.setBlockSize(wd.getBlockWidth(3), wd.getBlockHeight(1))
                   .setShowAxisX(wd.getOptions().isShowAxisX())
                   .setShowAxisY(wd.getOptions().isShowAxisY())
                   .setShowChartFullScreenButton(wd.getOptions().isShowChartFullScreenButton())
                   .setChartPointsPerHour(wd.getOptions().getPointsPerHour())
                   .setShowDynamicLine(wd.getOptions().isShowDynamicLine())
                   .setDynamicLineColor(wd.getOptions().getDynamicLineColor());
            request.getAttachToLayoutHandler().accept(builder);

            for (DeviceEndpoint series : barSeries) {
                builder.addSeries(series.getName(false), seriesBuilder ->
                    seriesBuilder.setChartDataSource(
                        TemplateWidgetBuilder.getSource(widgetRequest.getEntityContext(), series)));
            }
        });
    }

    @Override
    public int getWidgetHeight(MainWidgetRequest request) {
        return 1;
    }
}
