package org.touchhome.bundle.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.entity.widget.AggregationType;
import org.touchhome.bundle.api.model.OptionModel;

import java.util.List;
import java.util.function.Consumer;

public interface EntityContextWidget {

    @NotNull EntityContext getEntityContext();

    @NotNull List<OptionModel> getDashboardTabs();

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

    /**
     * // color widget has available fields: colors, icon, name, brightness, colorTemp, onOff
     */
    void createColorWidget(
            @NotNull String entityID,
            @NotNull Consumer<ColorWidgetBuilder> widgetBuilder);

    void createSimpleColorWidget(
            @NotNull String entityID,
            @NotNull Consumer<SimpleColorWidgetBuilder> widgetBuilder);

    /**
     * @param entityID      -unique cline chart entity id. Must starts with EntityContextWidget.LINE_CHART_WIDGET_PREFIX
     * @param widgetBuilder - chart builder
     */
    void createLineChartWidget(
            @NotNull String entityID,
            @NotNull Consumer<LineChartBuilder> widgetBuilder);


    interface LineChartBuilder {
        // void addLineChart(String color, HasTimeValueSeries lineChartSeries);

        // @NotNull Consumer<LineChartSeriesWidgetBuilder> seriesWidgetBuilder
    }

    interface LineChartSeriesWidgetBuilder extends WidgetBaseBuilder<LineChartSeriesWidgetBuilder> {

        LineChartSeriesWidgetBuilder showAxisX(boolean on);

        LineChartSeriesWidgetBuilder showAxisY(boolean on);

        LineChartSeriesWidgetBuilder axisLabelX(@Nullable String name);

        LineChartSeriesWidgetBuilder axisLabelY(@Nullable String name);
    }

    interface ColorWidgetBuilder extends
            WidgetBaseBuilder<ColorWidgetBuilder>,
            HasIconWithoutThreshold<ColorWidgetBuilder>,
            HasLayout<ColorWidgetBuilder> {

        ColorWidgetBuilder setColors(String... colors);

        ColorWidgetBuilder setCircleSize(int value);

        ColorWidgetBuilder setIcon(@Nullable String icon);

        ColorWidgetBuilder setCircleSpacing(int value);

        ColorWidgetBuilder setColorValueDataSource(@Nullable String value);

        ColorWidgetBuilder setColorSetValueDataSource(@Nullable String value);

        ColorWidgetBuilder setBrightnessValueDataSource(@Nullable String value);

        ColorWidgetBuilder setBrightnessSetValueDataSource(@Nullable String value);

        ColorWidgetBuilder setBrightnessMinValue(int value);

        ColorWidgetBuilder setBrightnessMaxValue(int value);

        ColorWidgetBuilder setColorTemperatureValueDataSource(@Nullable String value);

        ColorWidgetBuilder setColorTemperatureSetValueDataSource(@Nullable String value);

        ColorWidgetBuilder setColorTemperatureMinValue(int value);

        ColorWidgetBuilder setColorTemperatureMaxValue(int value);

        ColorWidgetBuilder setOnOffValueDataSource(@Nullable String value);

        ColorWidgetBuilder setOnOffSetValueDataSource(@Nullable String value);
    }

    interface SimpleColorWidgetBuilder extends
            WidgetBaseBuilder<SimpleColorWidgetBuilder>,
            HasSingleValueDataSource<SimpleColorWidgetBuilder>,
            HasAlign<SimpleColorWidgetBuilder> {

        SimpleColorWidgetBuilder setColors(String... colors);

        SimpleColorWidgetBuilder setCircleSize(int value);

        SimpleColorWidgetBuilder setCircleSpacing(int value);
    }

    interface LayoutWidgetBuilder extends WidgetBaseBuilder<LayoutWidgetBuilder> {
        // Default - (2x2). Max 8x8
        LayoutWidgetBuilder setLayoutDimension(int rows, int columns);

        LayoutWidgetBuilder setBorderColor(@Nullable String value);

        LayoutWidgetBuilder setShowWidgetBorders(boolean value);
    }

    interface SimpleValueWidgetBuilder extends
            WidgetBaseBuilder<SimpleValueWidgetBuilder>,
            HasIcon<SimpleValueWidgetBuilder>,
            HasPadding<SimpleValueWidgetBuilder>,
            HasSingleValueDataSource<SimpleValueWidgetBuilder>,
            HasAlign<SimpleValueWidgetBuilder>,
            HasValueConverter<SimpleValueWidgetBuilder>,
            HasValueTemplate<SimpleValueWidgetBuilder> {
    }

    interface HasAlign<T> {
        // default - left:center
        T setAlign(HorizontalAlign horizontalAlign, VerticalAlign verticalAlign);
    }

    interface DisplayWidgetBuilder extends
            WidgetBaseBuilder<DisplayWidgetBuilder>,
            HasName<DisplayWidgetBuilder>,
            HasPadding<DisplayWidgetBuilder>,
            HasLayout<DisplayWidgetBuilder>,
            HasChartDataSource<DisplayWidgetBuilder>,
            HasHorizontalLine<DisplayWidgetBuilder>,
            HasSourceServerUpdates<DisplayWidgetBuilder>,
            HasLineChartBehaviour<DisplayWidgetBuilder> {

        DisplayWidgetBuilder setChartHeight(int value);

        DisplayWidgetBuilder setBarBorderWidth(int top, int right, int bottom, int left);

        DisplayWidgetBuilder setChartType(@NotNull ChartType value);

        DisplayWidgetBuilder addSeries(@Nullable String name, @NotNull Consumer<DisplayWidgetSeriesBuilder> builder);
    }

    interface SliderWidgetBuilder extends
            WidgetBaseBuilder<SliderWidgetBuilder>,
            HasName<SliderWidgetBuilder>,
            HasPadding<SliderWidgetBuilder>,
            HasLayout<SliderWidgetBuilder>,
            HasSourceServerUpdates<SliderWidgetBuilder> {

        SliderWidgetBuilder addSeries(@Nullable String name, @NotNull Consumer<SliderWidgetSeriesBuilder> builder);
    }

    interface SimpleToggleWidgetBuilder extends
            WidgetBaseBuilder<SimpleToggleWidgetBuilder>,
            HasToggle<SimpleToggleWidgetBuilder>,
            HasAlign<SimpleToggleWidgetBuilder>,
            HasPadding<SimpleToggleWidgetBuilder>,
            HasSingleValueDataSource<SimpleToggleWidgetBuilder>,
            HasSourceServerUpdates<SimpleToggleWidgetBuilder> {
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

    interface SliderWidgetSeriesBuilder extends
            HasIcon<SliderWidgetSeriesBuilder>,
            HasValueTemplate<SliderWidgetSeriesBuilder>,
            HasName<SliderWidgetSeriesBuilder>,
            HasPadding<SliderWidgetSeriesBuilder>,
            HasSingleValueDataSource<SliderWidgetSeriesBuilder> {
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

    interface DisplayWidgetSeriesBuilder extends
            HasIcon<DisplayWidgetSeriesBuilder>,
            HasValueTemplate<DisplayWidgetSeriesBuilder>,
            HasName<DisplayWidgetSeriesBuilder>,
            HasValueConverter<DisplayWidgetSeriesBuilder>,
            HasSingleValueAggregatedDataSource<DisplayWidgetSeriesBuilder> {
    }

    interface ToggleWidgetSeriesBuilder extends
            HasIcon<ToggleWidgetSeriesBuilder>,
            HasName<ToggleWidgetSeriesBuilder>,
            HasToggle<ToggleWidgetSeriesBuilder>,
            HasSingleValueDataSource<ToggleWidgetSeriesBuilder> {
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

    interface HasValueConverter<T> {

        // Default - 'return value;'
        T setValueConverter(@Nullable String value);

        // Default - 0. range: 0..60 seconds
        // Update value on UI with using converter
        T setValueConverterRefreshInterval(int value);
    }

    interface HasValueTemplate<T> {

        // Default - ''
        T setValueTemplate(@Nullable String prefix, @Nullable String suffix);

        T setValueColor(@Nullable String value);

        // Default - '-'
        T setNoValueText(@Nullable String value);

        // Default - 1.0 Range: 0.1...2.0
        T setValueFontSize(double value);

        // Default - 1.0 Range: 0.1...2.0
        T setValuePrefixFontSize(double value);

        // Default - 1.0 Range: 0.1...2.0
        T setValueSuffixFontSize(double value);

        // Default - middle
        T setValueVerticalAlign(VerticalAlign value);

        // Default - middle
        T setValuePrefixVerticalAlign(VerticalAlign value);

        // Default - middle
        T setValueSuffixVerticalAlign(VerticalAlign value);

        T setValuePrefixColor(String value);

        T setValueSuffixColor(String value);
    }

    interface HasSingleValueAggregatedDataSource<T> extends HasSingleValueDataSource<T> {
        T setValueAggregationType(AggregationType value);

        T setValueAggregationPeriod(int value);
    }

    interface HasSingleValueDataSource<T> {
        T setValueDataSource(@Nullable String value);

        T setSetValueDataSource(@Nullable String value);
    }

    interface HasName<T> {
        T setName(@Nullable String value);

        T setShowName(boolean value);

        T setNameColor(@Nullable String value);
    }

    interface HasLayout<T> {
        T setLayout(@Nullable String value);
    }

    interface HasChartDataSource<T> {
        T setChartDataSource(@Nullable String value);

        T setChartAggregationType(AggregationType value);

        T setFinalChartValueConverter(@Nullable String value);

        T setChartColor(@Nullable String value);

        T setChartColorOpacity(int value);

        T setChartLabel(@Nullable String value);
    }

    interface HasLineChartBehaviour<T> {
        T setLineBorderWidth(int value);

        T setLineFill(Fill value);

        T setStepped(Stepped value);

        T setTension(int value);

        T setPointRadius(double value);

        T setPointStyle(PointStyle value);

        T setPointBackgroundColor(@Nullable String value);

        T setPointBorderColor(@Nullable String value);
    }

    interface WidgetBaseBuilder<T> {
        T setName(@Nullable String name);

        T setBackground(@Nullable String value);

        T attachToTab(@NotNull String tabName);

        T attachToLayout(@NotNull String layoutEntityID, int rowNum, int columnNum);

        T setBlockSize(int width, int height);
    }

    interface HasSourceServerUpdates<T> {
        // Default - true
        T setListenSourceUpdates(@Nullable Boolean value);

        // Default - false
        T setShowLastUpdateTimer(@Nullable Boolean value);
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

    interface ThresholdBuilder {
        //target i.e. icon/color,
        ThresholdBuilder setThreshold(String target, Object numValue, ValueCompare op);

        @RequiredArgsConstructor
        enum ValueCompare {
            gt(">"), lt("<"), eq("="), neq("!="), regexp("RegExp");
            @Getter
            private final String op;
        }
    }

    enum HorizontalAlign {
        left, center, right
    }

    enum VerticalAlign {
        top, middle, bottom
    }
}
