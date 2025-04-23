package org.homio.api.widget.template.impl;

import static org.homio.api.ui.field.UIFieldLayout.HorizontalAlign.left;
import static org.homio.api.ui.field.UIFieldLayout.HorizontalAlign.right;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.entity.HasPlace;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.ui.UI;
import org.homio.api.ui.field.UIFieldLayout;
import org.homio.api.widget.template.TemplateWidgetBuilder;
import org.homio.api.widget.template.WidgetDefinition;
import org.homio.api.widget.template.WidgetDefinition.ItemDefinition;

public class ToggleTemplateWidget implements TemplateWidgetBuilder {

    @Override
    public void buildWidget(WidgetRequest widgetRequest) {
        // Use Compose Widget instead
        throw new NotImplementedException();
    }

    @Override
    public int getWidgetHeight(MainWidgetRequest request) {
        int defaultWidgetHeight = getWidgetHeight(request.getItemIncludeEndpoints());
        return request.getItem().getWidgetHeight(defaultWidgetHeight);
    }

    @Override
    public void buildMainWidget(MainWidgetRequest request) {
        WidgetRequest widgetRequest = request.getWidgetRequest();
        DeviceEndpointsBehaviourContract entity = widgetRequest.entity();

        Map<String, ? extends DeviceEndpoint> endpoints = entity.getDeviceEndpoints();
        List<DeviceEndpoint> includeEndpoints = request.getItemIncludeEndpoints();
        if (includeEndpoints.isEmpty()) {
            throw new IllegalArgumentException("Unable to find endpoints from device: " + entity);
        }
        WidgetDefinition wd = request.getItem();

        widgetRequest.context().widget().createToggleWidget("tgl_" + entity.getEntityID(), builder -> {
            TemplateWidgetBuilder.buildCommon(wd, widgetRequest, builder);
            builder.setDisplayType(wd.getOptions().getToggleType())
                    .setLayout(UIFieldLayout.LayoutBuilder.builder(50, 50).addRow(
                            rowBuilder -> rowBuilder.addCol("name", left).addCol("button", right)
                    ).build());
            builder.setBlockSize(
                    wd.getBlockWidth(request.getLayoutColumnNum()),
                    wd.getBlockHeight(request.getLayoutRowNum()));
            builder.setShowAllButton(wd.getOptions().getShowAllButton());
            request.getAttachToLayoutHandler().accept(builder);

            for (DeviceEndpoint endpoint : includeEndpoints) {
                ItemDefinition wbEndpoint = wd.getEndpoint(endpoint.getEndpointEntityID());
                DeviceEndpoint deviceEndpoint = endpoints.get(endpoint.getEndpointEntityID());
                builder.addSeries(getName(entity, deviceEndpoint), seriesBuilder -> {
                    TemplateWidgetBuilder.buildIconAndColor(endpoint, seriesBuilder, wbEndpoint, widgetRequest);
                    TemplateWidgetBuilder.setValueDataSource(seriesBuilder, widgetRequest.context(), deviceEndpoint)
                            .setColor(UI.Color.random());
                });
            }
        });
    }

    private int getWidgetHeight(List<DeviceEndpoint> endpoints) {
        return Math.round(endpoints.size() * 2F / 3);
    }

    private String getName(DeviceEndpointsBehaviourContract entity, DeviceEndpoint state) {
        String place = entity instanceof HasPlace placeEntity ? placeEntity.getPlace() : null;
        if (StringUtils.isNotEmpty(place)) {
            return "%s[%s]".formatted(place, state.getName(true));
        }
        return state.getName(true);
    }
}
