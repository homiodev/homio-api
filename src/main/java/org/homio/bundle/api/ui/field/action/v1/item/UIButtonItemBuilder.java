package org.homio.bundle.api.ui.field.action.v1.item;

import org.homio.bundle.api.ui.field.action.v1.UIEntityItemBuilder;
import org.jetbrains.annotations.NotNull;

public interface UIButtonItemBuilder extends UIEntityItemBuilder<UIButtonItemBuilder, String> {
    UIButtonItemBuilder setText(@NotNull String text);

    UIButtonItemBuilder setPrimary(boolean primary);
}
