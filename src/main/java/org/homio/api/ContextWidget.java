package org.homio.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.entity.widget.AggregationType;
import org.homio.api.model.Icon;
import org.homio.api.model.OptionModel;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.homio.api.widget.JavaScriptBuilder;
import org.homio.api.widget.template.WidgetDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface ContextWidget {

    @NotNull
    Context context();

    void createCustomWidget(@NotNull String entityID,
                            @NotNull Consumer<CustomWidgetBuilder> widgetBuilder);

    @Deprecated
    void createWidgetTemplate(@NotNull String entityID, @NotNull String name, @NotNull ParentWidget parent,
                              @NotNull Icon icon, @NotNull Consumer<JavaScriptBuilder> jsBuilder);

    void createTemplateWidgetActions(
            @NotNull UIInputBuilder uiInputBuilder,
            @NotNull DeviceEndpointsBehaviourContract entity,
            @NotNull List<WidgetDefinition> widgets);

    // get dashboard tabs
    @NotNull
    List<OptionModel> getDashboardTabs();

    // Get default dashboard tab id
    @NotNull
    String getDashboardDefaultID();

    void createLayoutWidget(
            @NotNull String entityID,
            @NotNull Consumer<LayoutWidgetBuilder> widgetBuilder);

    void createDisplayWidget(
            @NotNull String entityID,
            @NotNull Consumer<DisplayWidgetBuilder> widgetBuilder);

    void createSliderWidget(
            @NotNull String entityID,
            @NotNull Consumer<SliderWidgetBuilder> widgetBuilder);

    void createSimpleValueWidget(
            @NotNull String entityID,
            @NotNull Consumer<SimpleValueWidgetBuilder> widgetBuilder);

    void createToggleWidget(
            @NotNull String entityID,
            @NotNull Consumer<ToggleWidgetBuilder> widgetBuilder);

    void createSimpleToggleWidget(
            @NotNull String entityID,
            @NotNull Consumer<SimpleToggleWidgetBuilder> widgetBuilder);

    // color widget has available fields: colors, icon, name, brightness, colorTemp, onOff
    void createColorWidget(
            @NotNull String entityID,
            @NotNull Consumer<ColorWidgetBuilder> widgetBuilder);

    void createSimpleColorWidget(
            @NotNull String entityID,
            @NotNull Consumer<SimpleColorWidgetBuilder> widgetBuilder);

    void createBarTimeChartWidget(
            @NotNull String entityID,
            @NotNull Consumer<BarTimeChartBuilder> widgetBuilder);

    void createLineChartWidget(
            @NotNull String entityID,
            @NotNull Consumer<LineChartBuilder> widgetBuilder);

    enum PointStyle {
        circle,
        cross,
        crossRot,
        dash,
        line,
        rect,
        rectRounded,
        rectRot,
        star,
        triangle
    }

    enum Stepped {
        False,
        True,
        Before,
        After,
        Middle
    }

    enum Fill {
        Start,
        End,
        Origin,
        Disabled,
        Stack
    }


    enum ChartType {
        line,
        bar
    }

    enum ToggleType {
        OnOff,
        Slide,
        SwitchGroup
    }

    enum SimpleToggleType {
        OnOff,
        Slide
    }

    enum PulseColor {
        black, red, blue, green, yellow
    }

    @Getter
    @RequiredArgsConstructor
    enum ValueCompare {
        gt(">"), lt("<"), eq("="), neq("!="), regexp("RegExp");
        private final String op;
    }

    enum BarChartType {
        Horizontal,
        Vertical
    }

    enum LegendPosition {
        top,
        right,
        bottom,
        left
    }

    enum LegendAlign {
        start,
        center,
        end
    }

    enum HorizontalAlign {
        left, center, right
    }

    enum VerticalAlign {
        top, middle, bottom
    }

    @Getter
    @RequiredArgsConstructor
    enum ParentWidget {
        Weather("fas fa-sun", "#BD9929"),
        Device("fas fa-microchip", "#3E74C4"),
        Media("fas fa-compact-disc", ""),
        Misc("fas fa-puzzle-piece fas", "#C45483");

        private final @NotNull String icon;
        private final @NotNull String color;
    }

    enum AxisDateFormat {
        auto,
        second,
        minute,
        hour,
        day,
        week,
        month,
        quarter,
        year
    }

    interface LineChartBuilder extends
            WidgetChartBaseBuilder<LineChartBuilder>,
            HasLegend<LineChartBuilder>,
            HasAxis<LineChartBuilder>,
            HasHorizontalLine<LineChartBuilder>,
            HasLineChartBehaviour<LineChartBuilder> {

        @NotNull
        LineChartBuilder addSeries(@Nullable String name, @NotNull Consumer<LineChartSeriesBuilder> builder);
    }

    interface LineChartSeriesBuilder extends
            HasChartDataSource<LineChartSeriesBuilder> {
    }

    interface BarTimeChartBuilder extends
            HasLegend<BarTimeChartBuilder>,
            WidgetChartBaseBuilder<BarTimeChartBuilder>,
            HasChartTimePeriod<BarTimeChartBuilder>,
            HasAxis<BarTimeChartBuilder>,
            HasHorizontalLine<BarTimeChartBuilder>,
            HasMinMaxChartValue<BarTimeChartBuilder> {

        @NotNull
        BarTimeChartBuilder addSeries(@Nullable String name, @NotNull Consumer<BarTimeChartSeriesBuilder> builder);

        // Default - ''
        @NotNull
        BarTimeChartBuilder setAxisLabel(@Nullable String value);

        // Default - BarChartType.Vertical
        @NotNull
        BarTimeChartBuilder setDisplayType(@Nullable BarChartType value);

        // Default -1x1x1x1
        @NotNull
        BarTimeChartBuilder setBarBorderWidth(@Nullable String value);
    }

    interface BarTimeChartSeriesBuilder extends
            HasChartDataSource<BarTimeChartSeriesBuilder> {
    }

    interface ColorWidgetBuilder extends
            WidgetBaseBuilder<ColorWidgetBuilder>,
            HasIconWithoutThreshold<ColorWidgetBuilder>,
            HasLayout<ColorWidgetBuilder> {

        // Default - "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#00FFFF", "#FFFFFF"
        @NotNull
        ColorWidgetBuilder setColors(@NotNull String... colors);

        // Default - 28. range: 10..40
        @NotNull
        ColorWidgetBuilder setCircleSize(int value);

        @NotNull
        ColorWidgetBuilder setIcon(@Nullable String icon);

        // Default - 14. range: 0..40
        @NotNull
        ColorWidgetBuilder setCircleSpacing(int value);

        // Require
        @NotNull
        ColorWidgetBuilder setColorValueDataSource(@Nullable String value);

        @NotNull
        ColorWidgetBuilder setColorSetValueDataSource(@Nullable String value);

        @NotNull
        ColorWidgetBuilder setBrightnessValueDataSource(@Nullable String value);

        @NotNull
        ColorWidgetBuilder setBrightnessSetValueDataSource(@Nullable String value);

        // Default - 0
        @NotNull
        ColorWidgetBuilder setBrightnessMinValue(int value);

        // Default - 255
        @NotNull
        ColorWidgetBuilder setBrightnessMaxValue(int value);

        @NotNull
        ColorWidgetBuilder setColorTemperatureValueDataSource(@Nullable String value);

        @NotNull
        ColorWidgetBuilder setColorTemperatureSetValueDataSource(@Nullable String value);

        // Default - 0
        @NotNull
        ColorWidgetBuilder setColorTemperatureMinValue(int value);

        // Default - 255
        @NotNull
        ColorWidgetBuilder setColorTemperatureMaxValue(int value);

        @NotNull
        ColorWidgetBuilder setOnOffValueDataSource(@Nullable String value);

        @NotNull
        ColorWidgetBuilder setOnOffSetValueDataSource(@Nullable String value);
    }

    interface HasAlign<T> {

        // default - left:center
        T setAlign(HorizontalAlign horizontalAlign, VerticalAlign verticalAlign);
    }

    interface SimpleColorWidgetBuilder extends
            WidgetBaseBuilder<SimpleColorWidgetBuilder>,
            HasSingleValueDataSource<SimpleColorWidgetBuilder>,
            HasSetSingleValueDataSource<SimpleColorWidgetBuilder>,
            HasAlign<SimpleColorWidgetBuilder> {

        // Default - "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#00FFFF", "#FFFFFF"
        @NotNull
        SimpleColorWidgetBuilder setColors(@NotNull String... colors);

        // Default - 28. range: 10..40
        @NotNull
        SimpleColorWidgetBuilder setCircleSize(int value);

        // Default - 14. range: 0..40
        @NotNull
        SimpleColorWidgetBuilder setCircleSpacing(int value);
    }

    interface LayoutWidgetBuilder extends WidgetBaseBuilder<LayoutWidgetBuilder> {

        // Default - (2x2). Max 8x8
        @NotNull
        LayoutWidgetBuilder setLayoutDimension(int rows, int columns);

        // Default as General setting: WidgetBorderColorMenuSetting
        @NotNull
        LayoutWidgetBuilder setBorderColor(@Nullable String value);
    }

    interface SimpleValueWidgetBuilder extends
            WidgetBaseBuilder<SimpleValueWidgetBuilder>,
            HasActionOnClick<SimpleValueWidgetBuilder>,
            HasIcon<SimpleValueWidgetBuilder>,
            HasMargin<SimpleValueWidgetBuilder>,
            HasSingleValueDataSource<SimpleValueWidgetBuilder>,
            HasAlign<SimpleValueWidgetBuilder>,
            HasValueConverter<SimpleValueWidgetBuilder>,
            HasValueTemplate<SimpleValueWidgetBuilder> {
    }

    interface SliderWidgetBuilder extends
            WidgetBaseBuilder<SliderWidgetBuilder>,
            HasName<SliderWidgetBuilder>,
            HasMargin<SliderWidgetBuilder>,
            HasLayout<SliderWidgetBuilder> {

        @NotNull
        SliderWidgetBuilder addSeries(@Nullable String name, @NotNull Consumer<SliderWidgetSeriesBuilder> builder);
    }

    interface ToggleWidgetBuilder extends
            WidgetBaseBuilder<ToggleWidgetBuilder>,
            HasName<ToggleWidgetBuilder>,
            HasMargin<ToggleWidgetBuilder>,
            HasLayout<ToggleWidgetBuilder> {

        // Default - false
        @NotNull
        ToggleWidgetBuilder setShowAllButton(@Nullable Boolean value);

        // Default - Slide
        @NotNull
        ToggleWidgetBuilder setDisplayType(@NotNull ToggleType value);

        @NotNull
        ToggleWidgetBuilder addSeries(@Nullable String name, @NotNull Consumer<ToggleWidgetSeriesBuilder> builder);
    }

    interface DisplayWidgetBuilder extends
            WidgetChartBaseBuilder<DisplayWidgetBuilder>,
            HasActionOnClick<DisplayWidgetBuilder>,
            HasName<DisplayWidgetBuilder>,
            HasMargin<DisplayWidgetBuilder>,
            HasLayout<DisplayWidgetBuilder>,
            HasChartDataSource<DisplayWidgetBuilder>,
            HasHorizontalLine<DisplayWidgetBuilder>,
            HasLineChartBehaviour<DisplayWidgetBuilder> {

        // Default - 30%
        @NotNull
        DisplayWidgetBuilder setChartHeight(int value);

        // Default - 0x0x0x0
        @NotNull
        DisplayWidgetBuilder setBarBorderWidth(int top, int right, int bottom, int left);

        // Default ChartType.line
        @NotNull
        DisplayWidgetBuilder setChartType(@NotNull ChartType value);

        @NotNull
        DisplayWidgetBuilder addSeries(@Nullable String name, @NotNull Consumer<DisplayWidgetSeriesBuilder> builder);
    }

    interface HasToggle<T> {

        // Default - random color
        @NotNull
        T setColor(@Nullable String value);

        // Default - On
        @NotNull
        T setOnName(@Nullable String value);

        // Default - ''
        @NotNull
        T setOnValues(@NotNull String... values);

        // Default - Off
        @NotNull
        T setOffName(@Nullable String value);

        // Default - 'false'
        @NotNull
        T setPushToggleOffValue(@Nullable String value);

        // Default - 'true'
        @NotNull
        T setPushToggleOnValue(@Nullable String value);
    }

    interface SimpleToggleWidgetBuilder extends
            WidgetBaseBuilder<SimpleToggleWidgetBuilder>,
            HasToggle<SimpleToggleWidgetBuilder>,
            HasAlign<SimpleToggleWidgetBuilder>,
            HasMargin<SimpleToggleWidgetBuilder>,
            HasSingleValueDataSource<SimpleToggleWidgetBuilder>,
            HasSetSingleValueDataSource<SimpleToggleWidgetBuilder> {
    }

    interface SliderWidgetSeriesBuilder extends
            HasIcon<SliderWidgetSeriesBuilder>,
            HasValueTemplate<SliderWidgetSeriesBuilder>,
            HasName<SliderWidgetSeriesBuilder>,
            HasMargin<SliderWidgetSeriesBuilder>,
            HasSingleValueDataSource<SliderWidgetSeriesBuilder>,
            HasSetSingleValueDataSource<SliderWidgetSeriesBuilder> {

        // Default - random
        @NotNull
        SliderWidgetSeriesBuilder setSliderColor(@Nullable String value);

        // Default - 0
        @NotNull
        SliderWidgetSeriesBuilder setMin(int value);

        // Default - 255
        @NotNull
        SliderWidgetSeriesBuilder setMax(int value);

        // Default - 1. Min - 1
        @NotNull
        SliderWidgetSeriesBuilder setStep(int value);
    }

    interface HasValueConverter<T> {

        // Default - 'return value;'
        @NotNull
        T setValueConverter(@Nullable String value);

        // Default - 0. range: 0..60 seconds
        // Update value on UI with using converter
        @NotNull
        T setValueConverterRefreshInterval(int value);
    }

    interface DisplayWidgetSeriesBuilder extends
            HasIcon<DisplayWidgetSeriesBuilder>,
            HasValueTemplate<DisplayWidgetSeriesBuilder>,
            HasName<DisplayWidgetSeriesBuilder>,
            HasValueConverter<DisplayWidgetSeriesBuilder>,
            HasSingleValueDataSource<DisplayWidgetSeriesBuilder> {

        @NotNull
        DisplayWidgetSeriesBuilder setStyle(@NotNull String... styles);
    }

    interface ToggleWidgetSeriesBuilder extends
            HasIcon<ToggleWidgetSeriesBuilder>,
            HasName<ToggleWidgetSeriesBuilder>,
            HasToggle<ToggleWidgetSeriesBuilder>,
            HasSingleValueDataSource<ToggleWidgetSeriesBuilder>,
            HasSetSingleValueDataSource<ToggleWidgetSeriesBuilder> {
    }

    interface HasValueTemplate<T> {

        // Default - ''
        @NotNull
        T setValueTemplate(@Nullable String prefix, @Nullable String suffix);

        @NotNull
        T setValueColor(@Nullable String value);

        // Default - '-'
        @NotNull
        T setNoValueText(@Nullable String value);

        // Default - 1.0 Range: 0.1..2.0
        @NotNull
        T setValueFontSize(double value);

        // Default - 1.0 Range: 0.1..2.0
        @NotNull
        T setValuePrefixFontSize(double value);

        // Default - 1.0 Range: 0.1..2.0
        @NotNull
        T setValueSuffixFontSize(double value);

        // Default - middle
        @NotNull
        T setValueVerticalAlign(@NotNull VerticalAlign value);

        // Default - middle
        @NotNull
        T setValuePrefixVerticalAlign(@NotNull VerticalAlign value);

        // Default - middle
        @NotNull
        T setValueSuffixVerticalAlign(@NotNull VerticalAlign value);

        // Default - null
        @NotNull
        T setValuePrefixColor(@Nullable String value);

        // Default - null
        @NotNull
        T setValueSuffixColor(@Nullable String value);

        // Default - false
        @NotNull
        T setValueSourceClickHistory(boolean value);
    }

    interface HasSingleValueDataSource<T> {

        @NotNull
        T setValueDataSource(@Nullable String value);
    }

    interface HasSetSingleValueDataSource<T> {

        @NotNull
        T setSetValueDataSource(@Nullable String value);
    }

    interface HasChartDataSource<T> {

        // Default - true
        @NotNull
        T setSmoothing(boolean value);

        // Default - ''. require
        @NotNull
        T setChartDataSource(@Nullable String value);

        // Default - AverageNoZero
        @NotNull
        T setChartAggregationType(@NotNull AggregationType value);

        // Default - return value;
        @NotNull
        T setFinalChartValueConverter(@Nullable String value);

        // Default - random()
        @NotNull
        T setChartColor(@Nullable String value);

        // Default 50. range: 25..100
        @NotNull
        T setChartColorOpacity(int value);

        // Default - ''
        @NotNull
        T setChartLabel(@Nullable String value);

        // Default - false
        @NotNull
        T setFillEmptyValues(boolean value);
    }

    interface HasName<T> {

        @NotNull
        T setName(@Nullable String value);

        @NotNull
        T setNameColor(@Nullable String value);
    }

    interface HasLineChartBehaviour<T> extends HasMinMaxChartValue<T>, HasChartTimePeriod<T> {

        // Default - 2. range: 0..10
        @NotNull
        T setLineBorderWidth(int value);

        // Default - Fill.Origin
        @NotNull
        T setLineFill(@NotNull Fill value);

        // Default - false
        @NotNull
        T setStepped(@NotNull Stepped value);

        // Default - 4. range: 0..10
        @NotNull
        T setTension(int value);

        // Default - 0D. range: 0..4
        @NotNull
        T setPointRadius(double value);

        // Default - PointStyle.circle
        @NotNull
        T setPointStyle(@NotNull PointStyle value);

        // Default - white
        @NotNull
        T setPointBackgroundColor(@Nullable String value);

        // Default - PRIMARY_COLOR
        @NotNull
        T setPointBorderColor(@Nullable String value);
    }

    interface WidgetChartBaseBuilder<T> extends WidgetBaseBuilder<T> {

        // Default - true
        @NotNull
        T setShowChartFullScreenButton(boolean value);

        // Default - 60sec. range: 10..600
        // @NotNull
        // T setFetchDataFromServerInterval(int value);
    }

    interface HasLegend<T> {

        // Default - false
        @NotNull
        T setShowLegend(@Nullable Boolean value);

        // Default - LegendPosition.top
        @NotNull
        T setLegendPosition(@NotNull LegendPosition value);

        // Default - LegendAlign.center
        @NotNull
        T setLegendAlign(@NotNull LegendAlign value);
    }

    interface HasHorizontalLine<T> {

        // Default - 0. min = 0, max = 100
        @NotNull
        T setSingleLinePos(@Nullable Integer value);

        // Default - red
        @NotNull
        T setSingleLineColor(@Nullable String value);

        // Default - 0 min = 0, max = 10
        @NotNull
        T setSingleLineWidth(@Nullable Integer value);

        // Default - green
        @NotNull
        T setDynamicLineColor(@Nullable String value);

        // Default - 0
        @NotNull
        T setDynamicLineWidth(@Nullable Integer value);
    }

    interface HasIconWithoutThreshold<T> {

        @NotNull
        T setIcon(@Nullable String icon);

        @NotNull
        T setIconColor(@Nullable String color);
    }

    interface HasMargin<T> {

        @NotNull
        T setMargin(int top, int right, int bottom, int left);
    }

    interface HasIcon<T> {

        @NotNull
        T setIcon(@Nullable String icon, @Nullable Consumer<ThresholdBuilder> iconBuilder);

        default @NotNull T setIcon(@Nullable String icon) {
            return setIcon(icon, null);
        }

        @NotNull
        T setIconColor(@Nullable String color, @Nullable Consumer<ThresholdBuilder> colorBuilder);

        default @NotNull T setIconColor(@Nullable String color) {
            return setIconColor(color, null);
        }

        default @NotNull T setIcon(@Nullable Icon icon) {
            if (icon != null) {
                setIcon(icon.getIcon());
                setIconColor(icon.getColor());
            }
            return (T) this;
        }
    }

    interface HasLayout<T> {

        @NotNull
        T setLayout(@Nullable String value);
    }

    interface WidgetBaseBuilder<T> {

        // Default - 20. (15 for layout widget)
        @NotNull
        T setZIndex(int index);

        @NotNull
        T setName(@Nullable String name);

        @NotNull
        T setStyle(String... styles);

        // Default - transparent
        @NotNull
        T setBackground(@Nullable String backgroundColor,
                        @Nullable Consumer<ThresholdBuilder> colorThresholdBuilder,
                        @Nullable Consumer<PulseBuilder> pulseThresholdBuilder
        );

        default @NotNull T setBackground(@Nullable String icon) {
            return setBackground(icon, null, null);
        }

        @NotNull
        T attachToTab(@NotNull String tabName);

        @NotNull
        T attachToLayout(@NotNull String layoutEntityID, int rowNum, int columnNum);

        // Default - 1x1
        @NotNull
        T setBlockSize(int width, int height);
    }

    interface HasActionOnClick<T> {

        @NotNull
        T setValueToPushSource(@Nullable String value);

        @NotNull
        T setValueOnClick(@Nullable String value);

        @NotNull
        T setValueOnDoubleClick(@Nullable String value);

        @NotNull
        T setValueOnHoldClick(@Nullable String value);

        @NotNull
        T setValueOnHoldReleaseClick(@Nullable String value);

        @NotNull
        T setValueToPushConfirmMessage(@Nullable String value);
    }

    interface HasMinMaxChartValue<T> {

        // Default - null
        @NotNull
        T setMin(@Nullable Integer value);

        // Default - null
        @NotNull
        T setMax(@Nullable Integer value);
    }

    interface HasChartTimePeriod<T> {

        // Default - 60
        @NotNull
        T setChartMinutesToShow(int value);

        // Default - 60. range: 1..600
        @NotNull
        T setChartPointsPerHour(int value);
    }

    interface HasAxis<T> {

        // Default - true
        @NotNull
        T setShowAxisX(@Nullable Boolean value);

        // Default - true
        @NotNull
        T setShowAxisY(@Nullable Boolean value);

        // Default - ''
        @NotNull
        T setAxisLabelX(@Nullable String value);

        // Default - ''
        @NotNull
        T setAxisLabelY(@Nullable String value);

        // Default - ''
        @NotNull
        T setAxisDateFormat(@Nullable AxisDateFormat value);
    }

    interface ThresholdBuilder {

        /**
         * Set threshold
         *
         * @param entity   icon or color
         * @param numValue value to compare to. May be number or string
         * @param op       compare operator
         * @param source   source variable as source to compare
         * @return this
         */
        @NotNull
        ThresholdBuilder setThreshold(
                @NotNull String entity,
                @NotNull Object numValue,
                @NotNull ValueCompare op,
                @Nullable String source);

        default @NotNull ThresholdBuilder setThreshold(
                @NotNull String entity,
                @NotNull Object numValue,
                @NotNull ValueCompare op) {
            return setThreshold(entity, numValue, op, null);
        }
    }

    interface CustomWidgetBuilder {
        @NotNull
        CustomWidgetBuilder css(@NotNull String value);

        @NotNull
        default CustomWidgetBuilder css(@NotNull List<String> lines) {
            return css(String.join(System.lineSeparator(), lines));
        }

        @NotNull
        CustomWidgetBuilder code(@NotNull String value);

        @NotNull
        default CustomWidgetBuilder code(@NotNull List<String> lines) {
            return code(String.join(System.lineSeparator(), lines));
        }

        @NotNull
        CustomWidgetBuilder parameterEntity(@NotNull String entityID);

        // set value directly to widget. Useful when set non-default value from CustomWidgetConfigurableEntity
        @NotNull
        CustomWidgetBuilder setValue(@NotNull String key, @NotNull String value);
    }

    interface PulseBuilder {

        /**
         * @param pulseColor - pulse color
         * @param numValue   value to compare to. May be number or string
         * @param op         compare operator
         * @param source     source variable as source to compare
         * @return this
         */
        @NotNull
        PulseBuilder setPulse(
                @NotNull PulseColor pulseColor,
                @NotNull Object numValue,
                @NotNull ValueCompare op,
                @NotNull String source);
    }
}
