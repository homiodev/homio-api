package org.homio.api.widget.template.impl;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.Context;
import org.homio.api.ContextWidget.*;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.ui.UI;
import org.homio.api.widget.template.TemplateWidgetBuilder;
import org.homio.api.widget.template.WidgetDefinition;
import org.homio.api.widget.template.WidgetDefinition.ItemDefinition;
import org.homio.api.widget.template.WidgetDefinition.Options;
import org.homio.api.widget.template.WidgetDefinition.Options.Chart;
import org.homio.api.widget.template.WidgetDefinition.Options.Source;
import org.jetbrains.annotations.NotNull;

public class DisplayTemplateWidget implements TemplateWidgetBuilder {

  public static void fillHasLineChartBehaviour(HasLineChartBehaviour builder, Chart chart) {
    builder.setStepped(chart.getStepped());
    builder.setLineFill(chart.getFill());
    builder.setLineBorderWidth(chart.getLineBorderWidth());
    builder.setMin(chart.getMin());
    builder.setMax(chart.getMax());
  }

  public static void fillHasChartDataSource(HasChartDataSource builder, Chart chart) {
    builder.setChartColorOpacity(chart.getOpacity());
    builder.setChartAggregationType(chart.getAggregateFunc());
    builder.setSmoothing(chart.isSmoothing());
    builder.setChartColor(StringUtils.defaultIfEmpty(chart.getColor(), UI.Color.random()));
    builder.setFillEmptyValues(chart.isFillEmptyValues());
  }

  @Override
  public void buildWidget(WidgetRequest widgetRequest) {
    Context context = widgetRequest.context();
    DeviceEndpointsBehaviourContract entity = widgetRequest.entity();
    WidgetDefinition wd = widgetRequest.widgetDefinition();

    int endpointCount = widgetRequest.includeEndpoints().size();
    if (endpointCount == 0) {
      throw new IllegalArgumentException("Unable to find display endpoints for device: " + entity);
    }

    String layoutID = "lt-dsp-" + entity.getIeeeAddress();
    Map<String, DeviceEndpoint> endpoints =
        entity.getDeviceEndpoints().entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    context
        .widget()
        .createLayoutWidget(
            layoutID,
            builder -> {
              TemplateWidgetBuilder.buildCommon(wd, widgetRequest, builder);
              builder
                  .setBlockSize(wd.getBlockWidth(1), wd.getBlockHeight(1))
                  .setLayoutDimension(endpointCount + 1, 3);
            });
    var request =
        new MainWidgetRequest(
            widgetRequest,
            wd,
            3,
            endpointCount + 1,
            builder -> builder.attachToLayout(layoutID, 0, 0));
    buildMainWidget(request);

    ComposeTemplateWidget.addBottomRow(context, wd, layoutID, endpointCount, endpoints);
  }

  @Override
  public void buildMainWidget(MainWidgetRequest request) {
    WidgetRequest widgetRequest = request.getWidgetRequest();
    Context context = widgetRequest.context();
    DeviceEndpointsBehaviourContract entity = widgetRequest.entity();

    List<DeviceEndpoint> includeEndpoints = request.getItemIncludeEndpoints();
    if (includeEndpoints.isEmpty()) {
      throw new IllegalArgumentException("Unable to find display endpoints for device: " + entity);
    }

    WidgetDefinition wd = request.getItem();
    context
        .widget()
        .createDisplayWidget(
            "dw-" + entity.getIeeeAddress(),
            builder -> {
              TemplateWidgetBuilder.buildCommon(wd, widgetRequest, builder);
              builder.setMargin(0, 2, 0, 2);
              buildPushValue(request.getItem().getOptions(), builder, entity, context);

              TemplateWidgetBuilder.buildBackground(wd.getBackground(), widgetRequest, builder);

              String layout = wd.getLayout();
              if (isNotEmpty(layout)) {
                builder.setLayout(layout);
              }
              builder.setBlockSize(
                  wd.getBlockWidth(request.getLayoutColumnNum()),
                  wd.getBlockHeight(request.getLayoutRowNum()));

              request.getAttachToLayoutHandler().accept(builder);

              for (DeviceEndpoint endpoint : includeEndpoints) {
                addEndpoint(
                    widgetRequest,
                    request.getItem(),
                    builder,
                    endpoint,
                    seriesBuilder ->
                        Optional.ofNullable(
                                ICON_ANIMATE_ENDPOINTS.get(endpoint.getEndpointEntityID()))
                            .ifPresent(ib -> ib.build(seriesBuilder)));
              }

              Chart chart = wd.getOptions().getChart();
              if (chart != null) {
                builder.setChartDataSource(
                    TemplateWidgetBuilder.buildDataSource(entity, context, chart.getSource()));
                builder.setChartHeight(chart.getHeight());
                fillHasChartDataSource(builder, chart);
                fillHasLineChartBehaviour(builder, chart);
              }
            });
  }

  @Override
  public int getWidgetHeight(MainWidgetRequest request) {
    return request.getItem().getWidgetHeight(request.getItemIncludeEndpoints().size());
  }

  private void buildPushValue(
      Options options,
      DisplayWidgetBuilder builder,
      DeviceEndpointsBehaviourContract entity,
      Context context) {
    builder.setValueOnClick(options.getValueOnClick());
    builder.setValueOnDoubleClick(options.getValueOnDoubleClick());
    builder.setValueOnHoldClick(options.getValueOnHoldClick());
    builder.setValueOnHoldReleaseClick(options.getValueOnHoldReleaseClick());

    builder.setValueToPushConfirmMessage(options.getPushConfirmMessage());
    Source pushSource = options.getPushSource();
    if (pushSource != null) {
      builder.setValueToPushSource(
          TemplateWidgetBuilder.buildDataSource(entity, context, pushSource));
    }
  }

  private void addEndpoint(
      @NotNull WidgetRequest widgetRequest,
      @NotNull WidgetDefinition wb,
      @NotNull DisplayWidgetBuilder builder,
      @NotNull DeviceEndpoint endpoint,
      @NotNull Consumer<DisplayWidgetSeriesBuilder> handler) {
    builder.addSeries(
        endpoint.getName(true),
        seriesBuilder -> {
          seriesBuilder
              .setValueDataSource(
                  TemplateWidgetBuilder.getSource(widgetRequest.context(), endpoint))
              .setValueTemplate(null, endpoint.getUnit())
              .setValueSuffixFontSize(0.6)
              .setValueSuffixColor("#777777")
              .setValueSuffixVerticalAlign(VerticalAlign.bottom);
          handler.accept(seriesBuilder);
          ItemDefinition itemDefinition = wb.getEndpoint(endpoint.getEndpointEntityID());
          if (itemDefinition != null) {
            seriesBuilder.setValueConverter(itemDefinition.getValueConverter());
            seriesBuilder.setValueConverterRefreshInterval(
                itemDefinition.getValueConverterRefreshInterval());
            seriesBuilder.setValueColor(itemDefinition.getValueColor());
            seriesBuilder.setValueSourceClickHistory(itemDefinition.isValueSourceClickHistory());
          }
          TemplateWidgetBuilder.buildIconAndColor(
              endpoint, seriesBuilder, itemDefinition, widgetRequest);
        });
  }
}
