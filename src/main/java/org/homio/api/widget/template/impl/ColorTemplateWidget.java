package org.homio.api.widget.template.impl;

import static org.homio.api.model.endpoint.DeviceEndpoint.ENDPOINT_SIGNAL;

import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;
import org.homio.api.Context;
import org.homio.api.ContextWidget.HorizontalAlign;
import org.homio.api.ContextWidget.VerticalAlign;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.widget.template.TemplateWidgetBuilder;
import org.homio.api.widget.template.WidgetDefinition;

public class ColorTemplateWidget implements TemplateWidgetBuilder {

    @Override
    public void buildWidget(WidgetRequest widgetRequest) {
        Context context = widgetRequest.context();
        DeviceEndpointsBehaviourContract entity = widgetRequest.entity();
        WidgetDefinition wd = widgetRequest.widgetDefinition();

        String layoutID = "lt-clr_" + entity.getIeeeAddress();
        Map<String, ? extends DeviceEndpoint> endpoints = entity.getDeviceEndpoints();
        DeviceEndpoint onOffEndpoint = endpoints.get("state");
        DeviceEndpoint brightnessEndpoint = endpoints.get("brightness");
        DeviceEndpoint colorEndpoint = endpoints.get("color");

        context.widget().createLayoutWidget(layoutID, builder -> {
            TemplateWidgetBuilder.buildCommon(wd, widgetRequest, builder);
            builder.setBlockSize(2, 1)
                    .setLayoutDimension(2, 6);
        });

        if (brightnessEndpoint != null) {
            context.widget().createSliderWidget("sl_" + entity.getIeeeAddress(), builder -> {
                builder.setBlockSize(wd.getBlockWidth(5), wd.getBlockHeight(1))
                        .setZIndex(wd.getZIndex(20));
                builder.attachToLayout(layoutID, 0, 0);
                builder.addSeries(entity.getModel(), seriesBuilder -> {
                    seriesBuilder.setIcon(entity.getEntityIcon());
                    TemplateWidgetBuilder.setValueDataSource(seriesBuilder, context, brightnessEndpoint);
                });
            });
        }

        context.widget().createSimpleColorWidget("clr_" + entity.getIeeeAddress(), builder -> {
            builder
                    .setBlockSize(5, 1)
                    .setZIndex(wd.getZIndex(20));
            TemplateWidgetBuilder.setValueDataSource(builder, context, colorEndpoint);
            builder.attachToLayout(layoutID, 1, 0);
        });

        if (onOffEndpoint != null) {
            context.widget().createSimpleToggleWidget("tgl-" + entity.getIeeeAddress(), builder -> {
                TemplateWidgetBuilder.setValueDataSource(builder, context, onOffEndpoint);
                builder.setAlign(HorizontalAlign.right, VerticalAlign.middle);
                builder.attachToLayout(layoutID, 0, 5);
            });
        }

        TemplateWidgetBuilder.addEndpoint(
                context,
                HorizontalAlign.right,
                endpoints.get(ENDPOINT_SIGNAL),
                false,
                builder -> builder.attachToLayout(layoutID, 1, 5));
    }

    @Override
    public int getWidgetHeight(MainWidgetRequest request) {
        throw new NotImplementedException();
    }

    @Override
    public void buildMainWidget(MainWidgetRequest request) {
        throw new NotImplementedException();
    }
}
