package org.touchhome.bundle.api.ui.field.action.v1.layout.dialog;

import org.jetbrains.annotations.NotNull;
import org.touchhome.bundle.api.ui.field.action.v1.UIEntityBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.item.UICheckboxItemBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.item.UIInfoItemBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.item.UITextInputItemBuilder;

import java.util.function.Consumer;

public interface UIDialogLayoutBuilder extends UIEntityBuilder {

    UIDialogLayoutBuilder setBackgroundColor(String backgroundColor);

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
