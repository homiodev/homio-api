package org.homio.api.ui.field.action.v1.item;

import org.homio.api.ui.field.action.v1.UIEntityItemBuilder;
import org.jetbrains.annotations.NotNull;

public interface UIButtonItemBuilder extends UIEntityItemBuilder<UIButtonItemBuilder, String> {
    UIButtonItemBuilder setText(@NotNull String text);

    // default - 32
    UIButtonItemBuilder setHeight(int height);

    UIButtonItemBuilder setPrimary(boolean primary);
}
