package org.homio.api;

import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.homio.api.entity.widget.AggregationType;
import org.homio.api.model.Icon;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EntityContextWidget {

    @NotNull EntityContext getEntityContext();

    // get dashboard tabs
    @NotNull List<OptionModel> getDashboardTabs();

    // Get default dashboard tab id
    @NotNull String getDashboardDefaultID();

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

    enum PulseColor {
        black, red, blue, green, yellow
    }

    @RequiredArgsConstructor
    enum ValueCompare {
        gt(">"), lt("<"), eq("="), neq("!="), regexp("RegExp");
        @Getter
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

    interface LineChartBuilder extends
            WidgetChartBaseBuilder<LineChartBuilder>,
            HasLegend<LineChartBuilder>,
            HasAxis<LineChartBuilder>,
            HasHorizontalLine<LineChartBuilder>,
            HasLineChartBehaviour<LineChartBuilder> {
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
        BarTimeChartBuilder addSeries(@Nullable String name, @NotNull Consumer<BarTimeChartSeriesBuilder> builder);

        // Default - ''
        BarTimeChartBuilder setAxisLabel(String value);

        // Default - BarChartType.Vertical
        BarTimeChartBuilder setDisplayType(BarChartType value);

        // Default -1x1x1x1
        BarTimeChartBuilder setBarBorderWidth(String value);
    }

    enum HorizontalAlign {
        left, center, right
    }

    interface BarTimeChartSeriesBuilder extends
            HasChartDataSource<BarTimeChartSeriesBuilder> {
    }

    enum VerticalAlign {
        top, middle, bottom
    }

    interface ColorWidgetBuilder extends
            WidgetBaseBuilder<ColorWidgetBuilder>,
            HasIconWithoutThreshold<ColorWidgetBuilder>,
            HasLayout<ColorWidgetBuilder> {

        // Default - "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#00FFFF", "#FFFFFF"
        ColorWidgetBuilder setColors(String... colors);

        // Default - 28. range: 10..40
        ColorWidgetBuilder setCircleSize(int value);

        ColorWidgetBuilder setIcon(@Nullable String icon);

        // Default - 14. range: 0..40
        ColorWidgetBuilder setCircleSpacing(int value);

        // Require
        ColorWidgetBuilder setColorValueDataSource(@Nullable String value);

        ColorWidgetBuilder setColorSetValueDataSource(@Nullable String value);

        ColorWidgetBuilder setBrightnessValueDataSource(@Nullable String value);

        ColorWidgetBuilder setBrightnessSetValueDataSource(@Nullable String value);

        // Default - 0
        ColorWidgetBuilder setBrightnessMinValue(int value);

        // Default - 255
        ColorWidgetBuilder setBrightnessMaxValue(int value);

        ColorWidgetBuilder setColorTemperatureValueDataSource(@Nullable String value);

        ColorWidgetBuilder setColorTemperatureSetValueDataSource(@Nullable String value);

        // Default - 0
        ColorWidgetBuilder setColorTemperatureMinValue(int value);

        // Default - 255
        ColorWidgetBuilder setColorTemperatureMaxValue(int value);

        ColorWidgetBuilder setOnOffValueDataSource(@Nullable String value);

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
        SimpleColorWidgetBuilder setColors(String... colors);

        // Default - 28. range: 10..40
        SimpleColorWidgetBuilder setCircleSize(int value);

        // Default - 14. range: 0..40
        SimpleColorWidgetBuilder setCircleSpacing(int value);
    }

    interface LayoutWidgetBuilder extends WidgetBaseBuilder<LayoutWidgetBuilder> {
        // Default - (2x2). Max 8x8
        LayoutWidgetBuilder setLayoutDimension(int rows, int columns);

        // Default as General setting: WidgetBorderColorMenuSetting
        LayoutWidgetBuilder setBorderColor(@Nullable String value);

        // Default - false
        LayoutWidgetBuilder setShowWidgetBorders(boolean value);
    }

    interface SimpleValueWidgetBuilder extends
            WidgetBaseBuilder<SimpleValueWidgetBuilder>,
            HasActionOnClick<SimpleValueWidgetBuilder>,
            HasIcon<SimpleValueWidgetBuilder>,
            HasPadding<SimpleValueWidgetBuilder>,
            HasSingleValueDataSource<SimpleValueWidgetBuilder>,
            HasAlign<SimpleValueWidgetBuilder>,
            HasValueConverter<SimpleValueWidgetBuilder>,
            HasValueTemplate<SimpleValueWidgetBuilder> {
    }

    interface SliderWidgetBuilder extends
            WidgetBaseBuilder<SliderWidgetBuilder>,
            HasName<SliderWidgetBuilder>,
            HasPadding<SliderWidgetBuilder>,
            HasLayout<SliderWidgetBuilder>,
            HasSourceServerUpdates<SliderWidgetBuilder> {

        SliderWidgetBuilder addSeries(@Nullable String name, @NotNull Consumer<SliderWidgetSeriesBuilder> builder);
    }

    interface ToggleWidgetBuilder extends
            WidgetBaseBuilder<ToggleWidgetBuilder>,
            HasName<ToggleWidgetBuilder>,
            HasPadding<ToggleWidgetBuilder>,
            HasLayout<ToggleWidgetBuilder>,
            HasSourceServerUpdates<ToggleWidgetBuilder> {

        // Default - false
        ToggleWidgetBuilder setShowAllButton(Boolean value);

        // Default - Slide
        ToggleWidgetBuilder setDisplayType(ToggleType value);

        ToggleWidgetBuilder addSeries(@Nullable String name, @NotNull Consumer<ToggleWidgetSeriesBuilder> builder);
    }

    interface DisplayWidgetBuilder extends
            WidgetChartBaseBuilder<DisplayWidgetBuilder>,
            HasActionOnClick<DisplayWidgetBuilder>,
            HasName<DisplayWidgetBuilder>,
            HasPadding<DisplayWidgetBuilder>,
            HasLayout<DisplayWidgetBuilder>,
            HasChartDataSource<DisplayWidgetBuilder>,
            HasHorizontalLine<DisplayWidgetBuilder>,
            HasSourceServerUpdates<DisplayWidgetBuilder>,
            HasLineChartBehaviour<DisplayWidgetBuilder> {

        // Default - 30%
        DisplayWidgetBuilder setChartHeight(int value);

        // Default - 0x0x0x0
        DisplayWidgetBuilder setBarBorderWidth(int top, int right, int bottom, int left);

        // Default ChartType.line
        DisplayWidgetBuilder setChartType(@NotNull ChartType value);

        DisplayWidgetBuilder setBackground(@Nullable String color,
                                           @Nullable Consumer<ThresholdBuilder> colorBuilder,
                                           @Nullable Consumer<PulseBuilder> animationBuilder);

        DisplayWidgetBuilder addSeries(@Nullable String name, @NotNull Consumer<DisplayWidgetSeriesBuilder> builder);
    }

    interface HasToggle<T> {
        // Default - random color
        T setColor(String value);

        // Default - On
        T setOnName(String value);

        // Default - ''
        T setOnValues(String... values);

        // Default - Off
        T setOffName(String value);

        // Default - 'false'
        T setPushToggleOffValue(String value);

        // Default - 'true'
        T setPushToggleOnValue(String value);
    }

    interface SimpleToggleWidgetBuilder extends
            WidgetBaseBuilder<SimpleToggleWidgetBuilder>,
            HasToggle<SimpleToggleWidgetBuilder>,
            HasAlign<SimpleToggleWidgetBuilder>,
            HasPadding<SimpleToggleWidgetBuilder>,
            HasSingleValueDataSource<SimpleToggleWidgetBuilder>,
            HasSetSingleValueDataSource<SimpleToggleWidgetBuilder>,
            HasSourceServerUpdates<SimpleToggleWidgetBuilder> {
    }

    interface SliderWidgetSeriesBuilder extends
            HasIcon<SliderWidgetSeriesBuilder>,
            HasValueTemplate<SliderWidgetSeriesBuilder>,
            HasName<SliderWidgetSeriesBuilder>,
            HasPadding<SliderWidgetSeriesBuilder>,
            HasSingleValueDataSource<SliderWidgetSeriesBuilder>,
            HasSetSingleValueDataSource<SliderWidgetSeriesBuilder> {
        // Default - random
        SliderWidgetSeriesBuilder setSliderColor(String value);

        // Default - 0
        SliderWidgetSeriesBuilder setMin(int value);

        // Default - 255
        SliderWidgetSeriesBuilder setMax(int value);

        // Default - 1. Min - 1
        SliderWidgetSeriesBuilder setStep(int value);

        // Default - 'return value;'
        SliderWidgetSeriesBuilder setTextConverter(String value);
    }

    interface HasValueConverter<T> {

        // Default - 'return value;'
        T setValueConverter(@Nullable String value);

        // Default - 0. range: 0..60 seconds
        // Update value on UI with using converter
        T setValueConverterRefreshInterval(int value);
    }

    interface HasSingleValueAggregatedDataSource<T> extends HasSingleValueDataSource<T> {
        T setValueAggregationType(AggregationType value);

        T setValueAggregationPeriod(int value);
    }

    interface DisplayWidgetSeriesBuilder extends
            HasIcon<DisplayWidgetSeriesBuilder>,
            HasValueTemplate<DisplayWidgetSeriesBuilder>,
            HasName<DisplayWidgetSeriesBuilder>,
            HasValueConverter<DisplayWidgetSeriesBuilder>,
            HasSingleValueAggregatedDataSource<DisplayWidgetSeriesBuilder> {
        DisplayWidgetSeriesBuilder setStyle(String... styles);
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
        T setValueTemplate(@Nullable String prefix, @Nullable String suffix);

        T setValueColor(@Nullable String value);

        // Default - '-'
        T setNoValueText(@Nullable String value);

        // Default - 1.0 Range: 0.1..2.0
        T setValueFontSize(double value);

        // Default - 1.0 Range: 0.1..2.0
        T setValuePrefixFontSize(double value);

        // Default - 1.0 Range: 0.1..2.0
        T setValueSuffixFontSize(double value);

        // Default - middle
        T setValueVerticalAlign(VerticalAlign value);

        // Default - middle
        T setValuePrefixVerticalAlign(VerticalAlign value);

        // Default - middle
        T setValueSuffixVerticalAlign(VerticalAlign value);

        // Default - null
        T setValuePrefixColor(String value);

        // Default - null
        T setValueSuffixColor(String value);

        // Default - false
        T setValueSourceClickHistory(boolean value);
    }

    interface HasSingleValueDataSource<T> {
        T setValueDataSource(@Nullable String value);
    }

    interface HasSetSingleValueDataSource<T> {
        T setSetValueDataSource(@Nullable String value);
    }

    interface HasChartDataSource<T> {

        // Default - true
        T setSmoothing(boolean value);

        // Default - ''. require
        T setChartDataSource(@Nullable String value);

        // Default - AverageNoZero
        T setChartAggregationType(AggregationType value);

        // Default - return value;
        T setFinalChartValueConverter(@Nullable String value);

        // Default - random()
        T setChartColor(@Nullable String value);

        // Default 50. range: 25..100
        T setChartColorOpacity(int value);

        // Default - ''
        T setChartLabel(@Nullable String value);

        // Default - false
        T setFillEmptyValues(boolean value);
    }

    interface HasName<T> {
        T setName(@Nullable String value);

        T setShowName(boolean value);

        T setNameColor(@Nullable String value);
    }

    interface HasLineChartBehaviour<T> extends HasMinMaxChartValue<T>, HasChartTimePeriod<T> {
        // Default - 2. range: 0..10
        T setLineBorderWidth(int value);

        // Default - Fill.Origin
        T setLineFill(Fill value);

        // Default - false
        T setStepped(Stepped value);

        // Default - 4. range: 0..10
        T setTension(int value);

        // Default - 0D. range: 0..4
        T setPointRadius(double value);

        // Default - PointStyle.circle
        T setPointStyle(PointStyle value);

        // Default - white
        T setPointBackgroundColor(@Nullable String value);

        // Default - PRIMARY_COLOR
        T setPointBorderColor(@Nullable String value);
    }

    interface WidgetChartBaseBuilder<T> extends WidgetBaseBuilder<T> {
        // Default - true
        T setShowChartFullScreenButton(boolean value);

        // Default - 60sec. range: 10..600
        T setFetchDataFromServerInterval(int value);
    }

    interface HasLegend<T> {
        // Default - false
        T setShowLegend(Boolean value);

        // Default - LegendPosition.top
        T setLegendPosition(LegendPosition value);

        // Default - LegendAlign.center
        T setLegendAlign(LegendAlign value);
    }

    interface HasHorizontalLine<T> {
        // Default - -1. min = -1, max = 100
        T setSingleLinePos(@Nullable Integer value);

        // Default - red
        T setSingleLineColor(@Nullable String value);

        // Default - 1 min = 1, max = 10
        T setSingleLineWidth(@Nullable Integer value);

        // Default - false
        T setShowDynamicLine(@Nullable Boolean value);

        // Default - green
        T setDynamicLineColor(@Nullable String value);

        // Default - 1
        T setDynamicLineWidth(@Nullable Integer value);
    }

    interface HasIconWithoutThreshold<T> {
        T setIcon(@Nullable String icon);

        T setIconColor(String color);
    }

    interface HasPadding<T> {
        T setPadding(int top, int right, int bottom, int left);
    }

    interface HasIcon<T> {
        T setIcon(@Nullable String icon, @Nullable Consumer<ThresholdBuilder> iconBuilder);

        default T setIcon(@Nullable String icon) {
            return setIcon(icon, null);
        }

        T setIconColor(@Nullable String color, @Nullable Consumer<ThresholdBuilder> colorBuilder);

        default T setIconColor(@Nullable String color) {
            return setIconColor(color, null);
        }

        default T setIcon(@Nullable Icon icon) {
            if (icon != null) {
                setIcon(icon.getIcon());
                setIconColor(icon.getColor());
            }
            return (T) this;
        }
    }

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
        Regular,
        Slide
    }

    interface HasLayout<T> {
        T setLayout(@Nullable String value);
    }

    interface HasSourceServerUpdates<T> {
        // Default - true
        T setListenSourceUpdates(@Nullable Boolean value);

        // Default - false
        T setShowLastUpdateTimer(@Nullable Boolean value);
    }

    interface WidgetBaseBuilder<T> {
        // Default - 20. (15 for layout widget)
        T setZIndex(int index);

        T setName(@Nullable String name);

        T setStyle(String... styles);

        // Default - transparent
        T setBackground(@Nullable String value);

        T attachToTab(@NotNull String tabName);

        T attachToLayout(@NotNull String layoutEntityID, int rowNum, int columnNum);

        // Default - 1x1
        T setBlockSize(int width, int height);
    }

    interface HasActionOnClick<T> {
        T setValueToPushSource(@Nullable String value);

        T setValueOnClick(@Nullable String value);

        T setValueOnDoubleClick(@Nullable String value);

        T setValueOnHoldClick(@Nullable String value);

        T setValueOnHoldReleaseClick(@Nullable String value);

        T setValueToPushConfirmMessage(@Nullable String value);
    }

    interface HasMinMaxChartValue<T> {
        // Default - null
        T setMin(Integer value);

        // Default - null
        T setMax(Integer value);
    }

    interface HasChartTimePeriod<T> {
        // Default - 60
        T setChartMinutesToShow(int value);

        // Default - 60. range: 1..600
        T setChartPointsPerHour(int value);
    }

    interface HasAxis<T> {
        // Default - true
        T setShowAxisX(Boolean value);

        // Default - true
        T setShowAxisY(Boolean value);

        // Default - ''
        T setAxisLabelX(String value);

        // Default - ''
        T setAxisLabelY(String value);

        // Default - ''
        T setAxisDateFormat(String value);
    }

    interface ThresholdBuilder {
        //target i.e. icon/color,
        ThresholdBuilder setThreshold(String entity, Object numValue, ValueCompare op);
    }

    interface PulseBuilder {
        PulseBuilder setPulse(PulseColor pulseColor, Object numValue, ValueCompare op);
    }
}
