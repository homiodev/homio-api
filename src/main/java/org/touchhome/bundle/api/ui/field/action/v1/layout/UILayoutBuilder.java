package org.touchhome.bundle.api.ui.field.action.v1.layout;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.touchhome.bundle.api.ui.action.UIActionHandler;
import org.touchhome.bundle.api.ui.field.action.v1.UIEntityBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.item.*;
import org.touchhome.bundle.api.ui.field.action.v1.layout.dialog.UIDialogLayoutBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.layout.dialog.UIStickyDialogItemBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface UILayoutBuilder extends UIEntityBuilder {
    @Unmodifiable
    Collection<UIEntityBuilder> getUiEntityBuilders(boolean flat);

    @Unmodifiable
    default Collection<UIEntityItemBuilder> getUiEntityItemBuilders(boolean flat) {
        return Collections.unmodifiableCollection(getUiEntityBuilders(flat).stream()
                .filter(ib -> ib instanceof UIEntityItemBuilder)
                .map(ib -> (UIEntityItemBuilder) ib).collect(Collectors.toList()));
    }

    String getStyle();

    @JsonIgnore
    int getNextOrder();

    UILayoutBuilder appendStyle(@NotNull String style, @NotNull String value);

    UILayoutBuilder removeStyle(@NotNull String style);

    void addRawUIEntityBuilder(@NotNull String name, UIEntityBuilder source);

    default UIFlexLayoutBuilder addFlex(@NotNull String name) {
        return addFlex(name, getNextOrder());
    }

    default UIFlexLayoutBuilder addFlex(@NotNull String name, Consumer<UIFlexLayoutBuilder> flexConsumer) {
        UIFlexLayoutBuilder flex = addFlex(name, getNextOrder());
        flexConsumer.accept(flex);
        return flex;
    }

    UIFlexLayoutBuilder addFlex(@NotNull String name, int order);

    default UIInfoItemBuilder addInfo(@NotNull String name) {
        return addInfo(name, UIInfoItemBuilder.InfoType.Text);
    }

    default UIInfoItemBuilder addInfo(@NotNull String name, int order) {
        return addInfo(name, UIInfoItemBuilder.InfoType.Text, order);
    }

    default UIInfoItemBuilder addInfo(@NotNull String name, UIInfoItemBuilder.InfoType infoType) {
        return addInfo(name, infoType, getNextOrder());
    }

    UIInfoItemBuilder addInfo(@NotNull String name, UIInfoItemBuilder.InfoType infoType, int order);

    /**
     * Add read-only duration that incremets on UI
     */
    void addDuration(long value, @Nullable String color);

    /**
     * Add read-write color picker
     */
    UIColorPickerItemBuilder addColorPicker(@NotNull String name, String color, UIActionHandler action);

    UITextInputItemBuilder addInput(@NotNull String name, String defaultValue,
                                    UITextInputItemBuilder.InputType inputType,
                                    boolean required);

    default UITextInputItemBuilder addTextInput(@NotNull String name, String defaultValue,
                                                boolean required) {
        return addInput(name, defaultValue, UITextInputItemBuilder.InputType.Text, required);
    }

    default UISelectBoxItemBuilder addSelectBox(@NotNull String name, UIActionHandler action) {
        return addSelectBox(name, action, getNextOrder());
    }

    UISelectBoxItemBuilder addSelectBox(@NotNull String name, UIActionHandler action, int order);

    default UICheckboxItemBuilder addCheckbox(@NotNull String name, boolean value, UIActionHandler action) {
        return addCheckbox(name, value, action, getNextOrder());
    }

    UICheckboxItemBuilder addCheckbox(@NotNull String name, boolean value, UIActionHandler action, int order);

    default UIMultiButtonItemBuilder addMultiButton(String name, UIActionHandler action) {
        return addMultiButton(name, action, getNextOrder());
    }

    UIMultiButtonItemBuilder addMultiButton(String name, UIActionHandler action, int order);

    default UISliderItemBuilder addSlider(@NotNull String name, float value, float min, float max, UIActionHandler action) {
        return addSlider(name, value, min, max, action, UISliderItemBuilder.SliderType.Regular, getNextOrder());
    }

    default UISliderItemBuilder addNumberInput(@NotNull String name, Float value, Float min, Float max,
                                               UIActionHandler action) {
        return addSlider(name, value, min, max, action, UISliderItemBuilder.SliderType.Input, getNextOrder());
    }

    UISliderItemBuilder addSlider(@NotNull String name, Float value, Float min, Float max,
                                  UIActionHandler action, UISliderItemBuilder.SliderType sliderType, int order);

    default UIButtonItemBuilder addButton(@NotNull String name, @Nullable String icon, @Nullable String iconColor,
                                          UIActionHandler action) {
        return addButton(name, icon, iconColor, action, getNextOrder());
    }

    UIButtonItemBuilder addButton(@NotNull String name, @Nullable String icon, @Nullable String iconColor,
                                  UIActionHandler action, int order);

    UIButtonItemBuilder addTableLayoutButton(@NotNull String name, int maxRows, int maxColumns, String value,
                                             @Nullable String icon, @Nullable String iconColor,
                                             UIActionHandler action, int order);

    default UIButtonItemBuilder addSimpleUploadButton(@NotNull String name, @Nullable String icon, @Nullable String iconColor,
                                                      String[] supportedFormats, UIActionHandler action) {
        return addSimpleUploadButton(name, icon, iconColor, supportedFormats, action, getNextOrder());
    }

    UIButtonItemBuilder addSimpleUploadButton(@NotNull String name, @Nullable String icon, @Nullable String iconColor,
                                              String[] supportedFormats, UIActionHandler action, int order);

    /**
     * text or icon must be not null!
     */
    default DialogEntity<UIStickyDialogItemBuilder> addStickyDialogButton(@NotNull String name, @Nullable String icon,
                                                                          @Nullable String iconColor) {
        return addStickyDialogButton(name, icon, iconColor, getNextOrder());
    }

    DialogEntity<UIStickyDialogItemBuilder> addStickyDialogButton(@NotNull String name, @Nullable String icon,
                                                                  @Nullable String iconColor, int order);

    /**
     * text or icon must be not null!
     */
    default DialogEntity<UIDialogLayoutBuilder> addOpenDialogActionButton(@NotNull String name, @Nullable String icon,
                                                                          @Nullable String iconColor, @Nullable Integer width) {
        return addOpenDialogActionButton(name, icon, iconColor, width, getNextOrder());
    }

    DialogEntity<UIDialogLayoutBuilder> addOpenDialogActionButton(@NotNull String name, @Nullable String icon,
                                                                  @Nullable String iconColor, @Nullable Integer width,
                                                                  int order);

    interface DialogEntity<D> {
        D up();

        D editButton(Consumer<UIButtonItemBuilder> editHandler);
    }
}
