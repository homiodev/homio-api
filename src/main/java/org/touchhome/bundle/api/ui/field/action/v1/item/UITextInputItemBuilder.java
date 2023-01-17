package org.touchhome.bundle.api.ui.field.action.v1.item;

import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

public interface UITextInputItemBuilder
        extends UIEntityItemBuilder<UITextInputItemBuilder, String> {

    enum InputType {
        Text,
        TextArea,
        Password,
        JSON,
        Ip
    }
}
