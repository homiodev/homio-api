package org.homio.api.widget.template.impl;

import static org.homio.api.model.endpoint.DeviceEndpoint.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.homio.api.Context;
import org.homio.api.ContextWidget.HorizontalAlign;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.widget.template.TemplateWidgetBuilder;
import org.homio.api.widget.template.WidgetDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ComposeTemplateWidget implements TemplateWidgetBuilder {

    public static final String[] LEFT_ENDPOINTS = new String[]{ENDPOINT_BATTERY, "power", "consumption", "energy", "voltage"};
    public static final String[] CENTER_ENDPOINTS = new String[]{ENDPOINT_LAST_SEEN};
    public static final String[] RIGHT_ENDPOINTS = new String[]{ENDPOINT_SIGNAL};

    public static void addBottomRow(Context context, WidgetDefinition wd, String layoutID, int row,
                                    Map<String, ? extends DeviceEndpoint> endpoints) {
        DeviceEndpoint leftEndpoint = findCellEndpoint(wd.getLeftEndpoint(), endpoints, LEFT_ENDPOINTS);
        TemplateWidgetBuilder.addEndpoint(
                context,
                HorizontalAlign.left,
                leftEndpoint,
                true,
                builder -> builder.attachToLayout(layoutID, row, 0));

        TemplateWidgetBuilder.addEndpoint(
                context,
                HorizontalAlign.center,
                findCellEndpoint(wd.getCenterEndpoint(), endpoints, CENTER_ENDPOINTS),
                false,
                builder -> builder.attachToLayout(layoutID, row, 1));

        TemplateWidgetBuilder.addEndpoint(
                context,
                HorizontalAlign.right,
                findCellEndpoint(wd.getRightEndpoint(), endpoints, RIGHT_ENDPOINTS),
                false,
                builder -> builder.attachToLayout(layoutID, row, 2));
    }

    private static @Nullable DeviceEndpoint findCellEndpoint(
            @Nullable String endpoint,
            @NotNull Map<String, ? extends DeviceEndpoint> endpoints,
            @NotNull String[] availableEndpoints) {
        if ("none".equals(endpoint)) {
            return null;
        }
        return Arrays.stream(availableEndpoints).filter(endpoints::containsKey).findFirst().map(endpoints::get).orElse(null);
    }

    @Override
    public void buildWidget(WidgetRequest widgetRequest) {
        Context context = widgetRequest.context();
        DeviceEndpointsBehaviourContract entity = widgetRequest.entity();
        WidgetDefinition wd = widgetRequest.widgetDefinition();
        List<WidgetDefinition> composeContainer = wd.getCompose();
        if (composeContainer == null || composeContainer.isEmpty()) {
            throw new IllegalArgumentException("Unable to create compose widget without compose endpoints");
        }

        // set 3 as min layout height to look better
        List<Integer> rowHeights = calcLayoutRows(widgetRequest, composeContainer);
        // 2
        int sumOfRowHeights = rowHeights.stream().reduce(0, Integer::sum);
        int sumOfRowHeightsAdjusted = Math.max(3, sumOfRowHeights);

        String layoutID = "lt-cmp-" + entity.getIeeeAddress();
        int columns = 3;

        context.widget().createLayoutWidget(layoutID, builder -> {
            TemplateWidgetBuilder.buildCommon(wd, widgetRequest, builder, 15);
            builder
                    .setBlockSize(
                            wd.getBlockWidth(1),
                            adjustBlockHeightToInnerContentHeight(wd, sumOfRowHeightsAdjusted))
                    .setLayoutDimension(sumOfRowHeightsAdjusted + 1, columns);
        });

        AtomicInteger currentLayoutRow = new AtomicInteger(0);
        for (int i = 0; i < composeContainer.size(); i++) {
            int wdMinRowHeight = rowHeights.get(i);
            int innerWidgetHeight = Math.round(wdMinRowHeight / (float) sumOfRowHeights * sumOfRowHeightsAdjusted);

            WidgetDefinition item = composeContainer.get(i);
            TemplateWidgetBuilder innerWidgetBuilder = TemplateWidgetBuilder.WIDGETS.get(item.getType());
            val request = new MainWidgetRequest(widgetRequest, item, columns, innerWidgetHeight, builder ->
                    builder.attachToLayout(layoutID, currentLayoutRow.get(), 0));
            innerWidgetBuilder.buildMainWidget(request);
            currentLayoutRow.addAndGet(sumOfRowHeightsAdjusted);
        }

        Map<String, ? extends DeviceEndpoint> endpoints = widgetRequest.entity().getDeviceEndpoints();

        addBottomRow(context, wd, layoutID, currentLayoutRow.get(), endpoints);
    }

    @Override
    public int getWidgetHeight(MainWidgetRequest request) {
        throw new NotImplementedException();
    }

    @Override
    public void buildMainWidget(MainWidgetRequest request) {
        throw new NotImplementedException();
    }

    private int adjustBlockHeightToInnerContentHeight(WidgetDefinition wd, int layoutRows) {
        int composeBlockHeight = wd.getBlockHeight(1);
        if (layoutRows > 6 && composeBlockHeight == 1) {
            composeBlockHeight = 2;
        }
        return composeBlockHeight;
    }

    private List<Integer> calcLayoutRows(WidgetRequest widgetRequest, List<WidgetDefinition> compose) {
        List<Integer> rowHeights = new ArrayList<>(compose.size());
        for (WidgetDefinition item : compose) {
            val request = new MainWidgetRequest(widgetRequest, item, 0, 0, null);
            rowHeights.add(TemplateWidgetBuilder.WIDGETS.get(item.getType()).getWidgetHeight(request));
        }
        return rowHeights;
    }
}
