package org.touchhome.bundle.api.ui.field.action.v1.item;

import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

public interface UIButtonItemBuilder extends UIEntityItemBuilder<UIButtonItemBuilder, String> {
    UIButtonItemBuilder setText(@Nullable String text);
}
