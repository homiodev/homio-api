package org.homio.bundle.api.ui.field.action.v1.item;

import org.homio.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

public interface UIMultiButtonItemBuilder
        extends UIEntityItemBuilder<UIMultiButtonItemBuilder, String> {

    UIMultiButtonItemBuilder addButton(String title);

    UIMultiButtonItemBuilder addButton(String title, String icon, String iconColor);

    UIMultiButtonItemBuilder setActive(String activeButton);
}
