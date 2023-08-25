package org.homio.api.widget.template;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.homio.api.model.endpoint.DeviceEndpoint.ENDPOINT_BATTERY;
import static org.homio.api.model.endpoint.DeviceEndpoint.ENDPOINT_HUMIDITY;
import static org.homio.api.model.endpoint.DeviceEndpoint.ENDPOINT_LAST_SEEN;
import static org.homio.api.model.endpoint.DeviceEndpoint.ENDPOINT_SIGNAL;
import static org.homio.api.model.endpoint.DeviceEndpoint.ENDPOINT_TEMPERATURE;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.homio.api.EntityContext;
import org.homio.api.EntityContextVar.VariableType;
import org.homio.api.EntityContextWidget;
import org.homio.api.EntityContextWidget.HasIcon;
import org.homio.api.EntityContextWidget.HasName;
import org.homio.api.EntityContextWidget.HasPadding;
import org.homio.api.EntityContextWidget.HasSetSingleValueDataSource;
import org.homio.api.EntityContextWidget.HasSingleValueDataSource;
import org.homio.api.EntityContextWidget.PulseBuilder;
import org.homio.api.EntityContextWidget.SimpleValueWidgetBuilder;
import org.homio.api.EntityContextWidget.ThresholdBuilder;
import org.homio.api.EntityContextWidget.VerticalAlign;
import org.homio.api.EntityContextWidget.WidgetBaseBuilder;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.widget.template.WidgetDefinition.ColorPicker;
import org.homio.api.widget.template.WidgetDefinition.IconPicker;
import org.homio.api.widget.template.WidgetDefinition.ItemDefinition;
import org.homio.api.widget.template.WidgetDefinition.Options.Pulse;
import org.homio.api.widget.template.WidgetDefinition.Options.Source;
import org.homio.api.widget.template.WidgetDefinition.Options.Threshold;
import org.homio.api.widget.template.WidgetDefinition.Padding;
import org.homio.api.widget.template.WidgetDefinition.WidgetType;
import org.homio.api.widget.template.impl.BarTimeTemplateWidget;
import org.homio.api.widget.template.impl.ColorTemplateWidget;
import org.homio.api.widget.template.impl.ComposeTemplateWidget;
import org.homio.api.widget.template.impl.DisplayTemplateWidget;
import org.homio.api.widget.template.impl.LineTemplateWidget;
import org.homio.api.widget.template.impl.ToggleTemplateWidget;
import org.homio.api.widget.template.impl.endpoints.BatteryIconEndpointBuilder;
import org.homio.api.widget.template.impl.endpoints.HumidityIconEndpointBuilder;
import org.homio.api.widget.template.impl.endpoints.IconEndpointBuilder;
import org.homio.api.widget.template.impl.endpoints.LastSeenIconEndpointBuilder;
import org.homio.api.widget.template.impl.endpoints.SignalIconEndpointBuilder;
import org.homio.api.widget.template.impl.endpoints.TemperatureIconEndpointBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        @NotNull EntityContext entityContext,
        @NotNull EntityContextWidget.HorizontalAlign horizontalAlign,
        @Nullable DeviceEndpoint endpoint,
        boolean addUnit,
        @NotNull Consumer<SimpleValueWidgetBuilder> attachHandler) {
        if (endpoint != null) {
            if (ENDPOINT_LAST_SEEN.equals(endpoint.getEndpointEntityID())) {
                createSimpleEndpoint(entityContext, horizontalAlign, endpoint, builder -> {
                    builder.setValueConverter("return Math.floor((new Date().getTime() - value) / 60000);");
                    builder.setValueConverterRefreshInterval(60);
                    buildValueSuffix(builder, "m");
                    attachHandler.accept(builder);
                }, false);
            } else {
                createSimpleEndpoint(entityContext, horizontalAlign, endpoint, attachHandler, addUnit);
            }
        }
    }

    static String getSource(EntityContext entityContext, DeviceEndpoint endpoint, boolean forSet) {
        return entityContext.var().buildDataSource(Objects.requireNonNull(endpoint.getVariableID()), forSet);
    }

    static <T extends HasSingleValueDataSource<?> & HasSetSingleValueDataSource<?>> T
    setValueDataSource(T builder, EntityContext entityContext, DeviceEndpoint endpoint) {
        builder.setValueDataSource(TemplateWidgetBuilder.getSource(entityContext, endpoint, false));
        builder.setSetValueDataSource(TemplateWidgetBuilder.getSource(entityContext, endpoint, true));
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
            iconBuilder.setIcon(defaultString(icon.getValue(), endpoint.getIcon().getIcon()),
                (Consumer<ThresholdBuilder>) iconThresholdBuilder ->
                    TemplateWidgetBuilder.buildThreshold(widgetRequest, icon.getThresholds(), iconThresholdBuilder));
            ColorPicker color = itemDefinition.getIconColor();
            if (color == null || (isEmpty(color.getValue()) && color.getThresholds() == null)) {
                return;
            }
            iconBuilder.setIconColor(defaultString(color.getValue(), endpoint.getIcon().getColor()),
                (Consumer<ThresholdBuilder>) thresholdBuilder ->
                    TemplateWidgetBuilder.buildThreshold(widgetRequest, color.getThresholds(), thresholdBuilder));
        }
    }

    static String buildDataSource(DeviceEndpointsBehaviourContract entity, EntityContext entityContext, Source source) {
        switch (source.getKind()) {
            case variable -> {
                String variable = entityContext.var().createVariable(entity.getEntityID(),
                    source.getValue(), source.getValue(), source.getVariableType(), null);
                return entityContext.var().buildDataSource(variable, true);
            }
            case broadcasts -> {
                String id = source.getValue() + "_" + entity.getIeeeAddress();
                String name = source.getValue() + " " + entity.getIeeeAddress();
                String variableID = entityContext.var().createVariable("broadcasts", id, name, VariableType.Any, null);
                return entityContext.var().buildDataSource(variableID, true);
            }
            case property -> {
                DeviceEndpoint endpoint = entity.getDeviceEndpoint(source.getValue());
                if (endpoint == null) {
                    throw new IllegalArgumentException("Unable to find device endpoint: " + source.getValue() +
                        " for device: " + entity);
                }
                return TemplateWidgetBuilder.getSource(entityContext, endpoint, true);
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
            nameBuilder.setName(widgetRequest.getEntity().getDescription());
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
                    buildDataSource(widgetRequest.getEntity(), widgetRequest.getEntityContext(), threshold.getSource()));
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
                    buildDataSource(widgetRequest.getEntity(), widgetRequest.getEntityContext(), pulse.getSource()));
            }
        }
    }

    void buildWidget(WidgetRequest widgetRequest);

    /**
     * Get total number of units for widget height
     */
    int getWidgetHeight(MainWidgetRequest request);

    void buildMainWidget(MainWidgetRequest request);

    private static void buildValueSuffix(SimpleValueWidgetBuilder builder, @Nullable String value) {
        builder.setValueTemplate(null, value)
               .setValueSuffixFontSize(0.6)
               .setValueSuffixColor("#777777")
               .setValueSuffixVerticalAlign(VerticalAlign.bottom);
    }

    private static void createSimpleEndpoint(
        @NotNull EntityContext entityContext,
        @NotNull EntityContextWidget.HorizontalAlign horizontalAlign,
        @NotNull DeviceEndpoint endpoint,
        @NotNull Consumer<SimpleValueWidgetBuilder> attachHandler,
        boolean addUnit) {
        entityContext.widget().createSimpleValueWidget(endpoint.getEntityID(), builder -> {
            builder.setIcon(endpoint.getIcon())
                   .setValueDataSource(getSource(entityContext, endpoint, false))
                   .setAlign(horizontalAlign, VerticalAlign.bottom)
                   .setValueFontSize(0.8);
            if (addUnit) {
                buildValueSuffix(builder, endpoint.getUnit());
            }
            Optional.ofNullable(ICON_ANIMATE_ENDPOINTS.get(endpoint.getEndpointEntityID())).ifPresent(ib -> ib.build(builder));
            attachHandler.accept(builder);
        });
    }

    @Getter
    @AllArgsConstructor
    class WidgetRequest {

        private final @NotNull EntityContext entityContext;
        private final @NotNull DeviceEndpointsBehaviourContract entity;
        private final @NotNull String tab;
        private final @NotNull WidgetDefinition widgetDefinition;
        private final @NotNull List<DeviceEndpoint> includeEndpoints;
    }

    @Getter
    @AllArgsConstructor
    class MainWidgetRequest {

        private final WidgetRequest widgetRequest;
        private final WidgetDefinition item;
        // total number of columns in layout
        private final int layoutColumnNum;
        private final int layoutRowNum;
        private Consumer<WidgetBaseBuilder> attachToLayoutHandler;

        public List<DeviceEndpoint> getItemIncludeEndpoints() {
            return item.getIncludeEndpoints(this);
        }
    }
}
