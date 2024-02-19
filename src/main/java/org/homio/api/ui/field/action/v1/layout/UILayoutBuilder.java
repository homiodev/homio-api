package org.homio.api.ui.field.action.v1.layout;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.homio.api.model.Icon;
import org.homio.api.ui.UIActionHandler;
import org.homio.api.ui.field.action.v1.UIEntityBuilder;
import org.homio.api.ui.field.action.v1.UIEntityItemBuilder;
import org.homio.api.ui.field.action.v1.item.UIButtonItemBuilder;
import org.homio.api.ui.field.action.v1.item.UICheckboxItemBuilder;
import org.homio.api.ui.field.action.v1.item.UIColorPickerItemBuilder;
import org.homio.api.ui.field.action.v1.item.UIIconPickerItemBuilder;
import org.homio.api.ui.field.action.v1.item.UIInfoItemBuilder;
import org.homio.api.ui.field.action.v1.item.UIMultiButtonItemBuilder;
import org.homio.api.ui.field.action.v1.item.UISelectBoxItemBuilder;
import org.homio.api.ui.field.action.v1.item.UISliderItemBuilder;
import org.homio.api.ui.field.action.v1.item.UITextInputItemBuilder;
import org.homio.api.ui.field.action.v1.layout.dialog.UIDialogLayoutBuilder;
import org.homio.api.ui.field.action.v1.layout.dialog.UIStickyDialogItemBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

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
     *
     * @param color -
     * @param value -
     */
    void addDuration(long value, @Nullable String color);

    /**
     * Add read-write color picker
     *
     * @param name   -
     * @param color  -
     * @param action -
     * @return -
     */
    UIColorPickerItemBuilder addColorPicker(@NotNull String name, String color, UIActionHandler action);

    default UIColorPickerItemBuilder addColorPicker(@NotNull String name, String color) {
        return addColorPicker(name, color, null);
    }

    UIIconPickerItemBuilder addIconPicker(@NotNull String name, String icon);

    UITextInputItemBuilder addInput(@NotNull String name, String defaultValue,
        UITextInputItemBuilder.InputType inputType,
        boolean required);

    default UITextInputItemBuilder addTextInput(@NotNull String name, String defaultValue,
        boolean required) {
        return addInput(name, defaultValue, UITextInputItemBuilder.InputType.Text, required);
    }

    default UISelectBoxItemBuilder addSelectBox(@NotNull String name, @Nullable UIActionHandler action) {
        return addSelectBox(name, action, getNextOrder());
    }

    default UISelectBoxItemBuilder addSelectBox(@NotNull String name) {
        return addSelectBox(name, null, getNextOrder());
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

    default UIButtonItemBuilder addButton(@NotNull String name, @Nullable Icon icon, UIActionHandler action) {
        return addButton(name, icon, action, getNextOrder());
    }

    UIButtonItemBuilder addButton(@NotNull String name, @Nullable Icon icon,
        UIActionHandler action, int order);

    UIButtonItemBuilder addTableLayoutButton(@NotNull String name, int maxRows, int maxColumns, String value,
        @Nullable Icon icon,
        UIActionHandler action, int order);

    default UIButtonItemBuilder addSimpleUploadButton(@NotNull String name, @Nullable Icon icon,
        String[] supportedFormats, UIActionHandler action) {
        return addSimpleUploadButton(name, icon, supportedFormats, action, getNextOrder());
    }

    UIButtonItemBuilder addSimpleUploadButton(@NotNull String name, @Nullable Icon icon,
        String[] supportedFormats, UIActionHandler action, int order);

    default DialogEntity<UIStickyDialogItemBuilder> addStickyDialogButton(@NotNull String name, @Nullable Icon icon) {
        return addStickyDialogButton(name, icon, getNextOrder());
    }

    DialogEntity<UIStickyDialogItemBuilder> addStickyDialogButton(@NotNull String name, @Nullable Icon icon, int order);

    // text or icon must be not null!
    default DialogEntity<UIDialogLayoutBuilder> addOpenDialogActionButton(@NotNull String name, @Nullable Icon icon,
        @Nullable Integer width) {
        return addOpenDialogActionButton(name, icon, width, getNextOrder());
    }

    DialogEntity<UIDialogLayoutBuilder> addOpenDialogActionButton(@NotNull String name, @Nullable Icon icon,
        @Nullable Integer width, int order);

    interface DialogEntity<D> {

        D up();

        D editButton(Consumer<UIButtonItemBuilder> editHandler);
    }
}
