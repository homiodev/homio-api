package org.touchhome.bundle.api.ui.field.action.v1.item;

import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

public interface UIMultiButtonItemBuilder
        extends UIEntityItemBuilder<UIMultiButtonItemBuilder, String> {
    UIMultiButtonItemBuilder addExtraButton(String title);

    UIMultiButtonItemBuilder addExtraButton(String title, String icon, String iconColor);

    UIMultiButtonItemBuilder setActive(String activeButton);
}
