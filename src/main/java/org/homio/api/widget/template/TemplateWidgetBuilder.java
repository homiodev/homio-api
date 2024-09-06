package org.homio.api.widget.template;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.homio.api.Context;
import org.homio.api.ContextVar.VariableType;
import org.homio.api.ContextWidget;
import org.homio.api.ContextWidget.*;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.widget.template.WidgetDefinition.*;
import org.homio.api.widget.template.WidgetDefinition.Options.Pulse;
import org.homio.api.widget.template.WidgetDefinition.Options.Source;
import org.homio.api.widget.template.WidgetDefinition.Options.Threshold;
import org.homio.api.widget.template.impl.*;
import org.homio.api.widget.template.impl.endpoints.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.homio.api.ContextVar.GROUP_BROADCAST;
import static org.homio.api.model.endpoint.DeviceEndpoint.*;

public interface TemplateWidgetBuilder {

    Map<WidgetType, TemplateWidgetBuilder> WIDGETS = Map.of(
            WidgetType.color, new ColorTemplateWidget(),
            WidgetType.toggle, new ToggleTemplateWidget(),
            WidgetType.display, new DisplayTemplateWidget(),
            WidgetType.compose, new ComposeTemplateWidget(),
            WidgetType.barTime, new BarTimeTemplateWidget(),
            WidgetType.line, new LineTemplateWidget()
    );

    Map<String, IconEndpointBuilder> ICON_ANIMATE_ENDPOINTS = Map.of(
            ENDPOINT_TEMPERATURE, new TemperatureIconEndpointBuilder(),
            ENDPOINT_HUMIDITY, new HumidityIconEndpointBuilder(),
            ENDPOINT_BATTERY, new BatteryIconEndpointBuilder(),
            ENDPOINT_SIGNAL, new SignalIconEndpointBuilder(),
            ENDPOINT_LAST_SEEN, new LastSeenIconEndpointBuilder()
    );

    static void addEndpoint(
            @NotNull Context context,
            @NotNull ContextWidget.HorizontalAlign horizontalAlign,
            @Nullable DeviceEndpoint endpoint,
            boolean addUnit,
            @NotNull Consumer<SimpleValueWidgetBuilder> attachHandler) {
        if (endpoint != null) {
            if (ENDPOINT_LAST_SEEN.equals(endpoint.getEndpointEntityID())) {
                createSimpleEndpoint(context, horizontalAlign, endpoint, builder -> {
                    builder.setValueConverter("return Math.floor((new Date().getTime() - value) / 60000);");
                    builder.setValueConverterRefreshInterval(60);
                    buildValueSuffix(builder, "m");
                    attachHandler.accept(builder);
                }, false);
            } else {
                createSimpleEndpoint(context, horizontalAlign, endpoint, attachHandler, addUnit);
            }
        }
    }

    static String getSource(Context context, DeviceEndpoint endpoint) {
        return context.var().buildDataSource(Objects.requireNonNull(endpoint.getVariableID()));
    }

    static <T extends HasSingleValueDataSource<?> & HasSetSingleValueDataSource<?>> T
    setValueDataSource(T builder, Context context, DeviceEndpoint endpoint) {
        builder.setValueDataSource(TemplateWidgetBuilder.getSource(context, endpoint));
        builder.setSetValueDataSource(TemplateWidgetBuilder.getSource(context, endpoint));
        return builder;
    }

    static void buildIconAndColor(DeviceEndpoint endpoint, HasIcon iconBuilder,
                                  ItemDefinition itemDefinition, WidgetRequest widgetRequest) {
        iconBuilder.setIcon(endpoint.getIcon());

        if (itemDefinition == null) {
            return;
        }
        IconPicker icon = itemDefinition.getIcon();
        if (icon != null) {
            iconBuilder.setIcon(Objects.toString(icon.getValue(), endpoint.getIcon().getIcon()),
                    (Consumer<ThresholdBuilder>) iconThresholdBuilder ->
                            TemplateWidgetBuilder.buildThreshold(widgetRequest, icon.getThresholds(), iconThresholdBuilder));
            ColorPicker color = itemDefinition.getIconColor();
            if (color == null || (isEmpty(color.getValue()) && color.getThresholds() == null)) {
                return;
            }
            iconBuilder.setIconColor(Objects.toString(color.getValue(), endpoint.getIcon().getColor()),
                    (Consumer<ThresholdBuilder>) thresholdBuilder ->
                            TemplateWidgetBuilder.buildThreshold(widgetRequest, color.getThresholds(), thresholdBuilder));
        }
    }

    static String buildDataSource(DeviceEndpointsBehaviourContract entity, Context context, Source source) {
        switch (source.getKind()) {
            case variable -> {
                String variable = context.var().createVariable(entity.getEntityID(),
                        source.getValue(), source.getValue(), source.getVariableType(), null).getId();
                return context.var().buildDataSource(variable);
            }
            case broadcast -> {
                String id = source.getValue() + "_" + entity.getIeeeAddress();
                String name = source.getValue() + " " + entity.getIeeeAddress();
                String variableID = context.var().createVariable(GROUP_BROADCAST, id, name, VariableType.Any, null)
                        .getId();
                return context.var().buildDataSource(variableID);
            }
            case property -> {
                DeviceEndpoint endpoint = entity.getDeviceEndpoint(source.getValue());
                if (endpoint == null) {
                    throw new IllegalArgumentException("Unable to find device endpoint: " + source.getValue() +
                                                       " for device: " + entity);
                }
                return TemplateWidgetBuilder.getSource(context, endpoint);
            }
            default -> throw new IllegalArgumentException("Unable to find handler for type: " + source.getKind());
        }
    }

    static void buildCommon(WidgetDefinition wd, WidgetRequest widgetRequest, WidgetBaseBuilder builder) {
        buildCommon(wd, widgetRequest, builder, 20);
    }

    static void buildCommon(WidgetDefinition wd, WidgetRequest widgetRequest, WidgetBaseBuilder builder, Integer defaultZIndex) {
        buildBackground(wd.getBackground(), widgetRequest, builder);
        builder.setZIndex(wd.getZIndex(defaultZIndex));
        if (builder instanceof HasName<?> nameBuilder) {
            nameBuilder.setName(widgetRequest.entity().getDescription());
            nameBuilder.setShowName(false);
        }
        Padding padding = wd.getPadding();
        if (padding != null && builder instanceof HasPadding<?> paddingBuilder) {
            paddingBuilder.setPadding(padding.getTop(), padding.getRight(),
                    padding.getBottom(), padding.getLeft());
        }
    }

    static void buildBackground(ColorPicker background, WidgetRequest widgetRequest, WidgetBaseBuilder builder) {
        if (background == null) {
            return;
        }
        builder.setBackground(background.getValue(),
                (Consumer<ThresholdBuilder>)
                        thresholdBuilder -> buildThreshold(widgetRequest, background.getThresholds(), thresholdBuilder),
                (Consumer<PulseBuilder>) pulseBuilder ->
                        buildPulseThreshold(widgetRequest, background.getPulses(), pulseBuilder));
    }

    static void buildThreshold(WidgetRequest widgetRequest, List<Threshold> thresholds, ThresholdBuilder thresholdBuilder) {
        if (thresholds != null) {
            for (Threshold threshold : thresholds) {
                thresholdBuilder.setThreshold(
                        threshold.getTarget(),
                        threshold.getValue(),
                        threshold.getOp(),
                        buildDataSource(widgetRequest.entity(), widgetRequest.context(), threshold.getSource()));
            }
        }
    }

    static void buildPulseThreshold(WidgetRequest widgetRequest, List<Pulse> pulses, PulseBuilder pulseThresholdBuilder) {
        if (pulses != null) {
            for (Pulse pulse : pulses) {
                pulseThresholdBuilder.setPulse(
                        pulse.getColor(),
                        pulse.getValue(),
                        pulse.getOp(),
                        buildDataSource(widgetRequest.entity(), widgetRequest.context(), pulse.getSource()));
            }
        }
    }

    private static void buildValueSuffix(SimpleValueWidgetBuilder builder, @Nullable String value) {
        builder.setValueTemplate(null, value)
                .setValueSuffixFontSize(0.6)
                .setValueSuffixColor("#777777")
                .setValueSuffixVerticalAlign(VerticalAlign.bottom);
    }

    private static void createSimpleEndpoint(
            @NotNull Context context,
            @NotNull ContextWidget.HorizontalAlign horizontalAlign,
            @NotNull DeviceEndpoint endpoint,
            @NotNull Consumer<SimpleValueWidgetBuilder> attachHandler,
            boolean addUnit) {
        context.widget().createSimpleValueWidget(endpoint.getEntityID(), builder -> {
            builder.setIcon(endpoint.getIcon())
                    .setValueDataSource(getSource(context, endpoint))
                    .setAlign(horizontalAlign, VerticalAlign.bottom)
                    .setValueFontSize(0.8);
            if (addUnit) {
                buildValueSuffix(builder, endpoint.getUnit());
            }
            Optional.ofNullable(ICON_ANIMATE_ENDPOINTS.get(endpoint.getEndpointEntityID())).ifPresent(ib -> ib.build(builder));
            attachHandler.accept(builder);
        });
    }

    void buildWidget(WidgetRequest widgetRequest);

    /**
     * Get total number of units for widget height
     */
    int getWidgetHeight(MainWidgetRequest request);

    void buildMainWidget(MainWidgetRequest request);

    record WidgetRequest(@NotNull Context context, @NotNull DeviceEndpointsBehaviourContract entity,
                         @NotNull String tab,
                         @NotNull WidgetDefinition widgetDefinition, @NotNull List<DeviceEndpoint> includeEndpoints) {

    }

    @Getter
    @AllArgsConstructor
    class MainWidgetRequest {

        private final @NotNull WidgetRequest widgetRequest;
        private final @NotNull WidgetDefinition item;
        // total number of columns in layout
        private final int layoutColumnNum;
        private final int layoutRowNum;
        private @NotNull Consumer<WidgetBaseBuilder> attachToLayoutHandler;

        public List<DeviceEndpoint> getItemIncludeEndpoints() {
            return item.getIncludeEndpoints(this);
        }
    }
}
