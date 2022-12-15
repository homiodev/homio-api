package org.touchhome.bundle.api.ui.field.action.v1.item;

import org.jetbrains.annotations.NotNull;
import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

public interface UIButtonItemBuilder extends UIEntityItemBuilder<UIButtonItemBuilder, String> {
    UIButtonItemBuilder setText(@NotNull String text);

    UIButtonItemBuilder setPrimary(boolean primary);
}
