package org.homio.api.widget.template.impl;

import static org.homio.api.model.endpoint.DeviceEndpoint.ENDPOINT_SIGNAL;

import java.util.Map;
import org.homio.api.EntityContext;
import org.homio.api.EntityContextWidget.HorizontalAlign;
import org.homio.api.EntityContextWidget.VerticalAlign;
import org.homio.api.entity.DeviceBaseEntity.HasEndpointsDevice;
import org.homio.api.exception.ProhibitedExecution;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.widget.template.TemplateWidgetBuilder;
import org.homio.api.widget.template.WidgetDefinition;

public class ColorTemplateWidget implements TemplateWidgetBuilder {

    @Override
    public void buildWidget(WidgetRequest widgetRequest) {
        EntityContext entityContext = widgetRequest.getEntityContext();
        HasEndpointsDevice entity = widgetRequest.getEntity();
        WidgetDefinition wd = widgetRequest.getWidgetDefinition();

        String layoutID = "lt-clr_" + entity.getIeeeAddress();
        Map<String, DeviceEndpoint> endpoints = entity.getDeviceEndpoints();
        DeviceEndpoint onOffEndpoint = endpoints.get("state");
        DeviceEndpoint brightnessEndpoint = endpoints.get("brightness");
        DeviceEndpoint colorEndpoint = endpoints.get("color");

        entityContext.widget().createLayoutWidget(layoutID, builder -> {
            TemplateWidgetBuilder.buildCommon(wd, widgetRequest, builder);
            builder.setBlockSize(2, 1)
                   .setLayoutDimension(2, 6);
        });

        if (brightnessEndpoint != null) {
            entityContext.widget().createSliderWidget("sl_" + entity.getIeeeAddress(), builder -> {
                builder.setBlockSize(wd.getBlockWidth(5), wd.getBlockHeight(1))
                       .setZIndex(wd.getZIndex(20));
                builder.attachToLayout(layoutID, 0, 0);
                builder.addSeries(entity.getModel(), seriesBuilder -> {
                    seriesBuilder.setIcon(entity.getEntityIcon());
                    TemplateWidgetBuilder.setValueDataSource(seriesBuilder, entityContext, brightnessEndpoint);
                });
            });
        }

        entityContext.widget().createSimpleColorWidget("clr_" + entity.getIeeeAddress(), builder -> {
            builder
                .setBlockSize(5, 1)
                .setZIndex(wd.getZIndex(20));
            TemplateWidgetBuilder.setValueDataSource(builder, entityContext, colorEndpoint);
            builder.attachToLayout(layoutID, 1, 0);
        });

        if (onOffEndpoint != null) {
            entityContext.widget().createSimpleToggleWidget("tgl-" + entity.getIeeeAddress(), builder -> {
                TemplateWidgetBuilder.setValueDataSource(builder, entityContext, onOffEndpoint);
                builder.setAlign(HorizontalAlign.right, VerticalAlign.middle);
                builder.attachToLayout(layoutID, 0, 5);
            });
        }

        TemplateWidgetBuilder.addEndpoint(
            entityContext,
            HorizontalAlign.right,
            endpoints.get(ENDPOINT_SIGNAL),
            false,
            builder -> builder.attachToLayout(layoutID, 1, 5));
    }

    @Override
    public int getWidgetHeight(MainWidgetRequest request) {
        throw new ProhibitedExecution();
    }

    @Override
    public void buildMainWidget(MainWidgetRequest request) {
        throw new ProhibitedExecution();
    }
}
