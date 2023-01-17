package org.touchhome.bundle.api.ui.field.action.v1.item;

import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

public interface UIColorPickerItemBuilder
        extends UIEntityItemBuilder<UIColorPickerItemBuilder, String> {

    // default is ColorPicker
    UIColorPickerItemBuilder setColorType(ColorType colorType);

    enum ColorType {
        ColorPicker,
        ColorSlider
    }
}
