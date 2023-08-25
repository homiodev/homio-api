package org.homio.api.ui.field.action.v1.item;

import org.homio.api.model.Icon;
import org.homio.api.ui.field.action.v1.UIEntityItemBuilder;
import org.jetbrains.annotations.NotNull;

public interface UIMultiButtonItemBuilder
    extends UIEntityItemBuilder<UIMultiButtonItemBuilder, String> {

    @NotNull UIMultiButtonItemBuilder addButton(@NotNull String title);

    @NotNull UIMultiButtonItemBuilder addButton(@NotNull String title, @NotNull Icon icon);

    @NotNull UIMultiButtonItemBuilder setActive(@NotNull String activeButton);
}
