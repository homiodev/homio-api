package org.touchhome.bundle.api.ui.field.action.v1.layout.dialog;

import org.jetbrains.annotations.NotNull;
import org.touchhome.bundle.api.ui.field.action.v1.UIEntityBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.item.UICheckboxItemBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.item.UIInfoItemBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.item.UISliderItemBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.item.UITextInputItemBuilder;

import java.util.function.Consumer;

public interface UIDialogLayoutBuilder extends UIEntityBuilder {

    DialogEntity<UIDialogLayoutBuilder> addFlex(@NotNull String name);

    default UIDialogLayoutBuilder setBackgroundColor(@NotNull String backgroundColor) {
        appendStyle("background", backgroundColor);
        return this;
    }

    String getStyle();

    UIDialogLayoutBuilder appendStyle(@NotNull String style, @NotNull String value);

    UIDialogLayoutBuilder setTitle(String title, String icon, String iconColor);

    default UIDialogLayoutBuilder setTitle(String title) {
        return setTitle(title, null, null);
    }

    default UIDialogLayoutBuilder setTitle(String title, String icon) {
        return setTitle(title, icon, null);
    }

    DialogEntity<UISliderItemBuilder> addSlider(@NotNull String name, float value, float min, float max);

    DialogEntity<UITextInputItemBuilder> addInput(@NotNull String name, String defaultValue,
                                                  UITextInputItemBuilder.InputType inputType,
                                                  boolean required);

    default DialogEntity<UITextInputItemBuilder> addTextInput(@NotNull String name, String defaultValue, boolean required) {
        return addInput(name, defaultValue, UITextInputItemBuilder.InputType.Text, required);
    }

    DialogEntity<UICheckboxItemBuilder> addCheckbox(@NotNull String name, boolean defaultValue);

    DialogEntity<UIInfoItemBuilder> addInfo(@NotNull String value, UIInfoItemBuilder.InfoType infoType);

    default DialogEntity<UIInfoItemBuilder> addInfo(@NotNull String value) {
        return addInfo(value, UIInfoItemBuilder.InfoType.Text);
    }

    interface DialogEntity<T> {
        UIDialogLayoutBuilder up();

        UIDialogLayoutBuilder edit(Consumer<T> editHandler);
    }
}
