package org.homio.api.ui.field.action.v1.item;

import org.homio.api.ui.UIActionHandler;
import org.homio.api.ui.field.action.v1.UIEntityItemBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UIIconPickerItemBuilder extends UIEntityItemBuilder<UIIconPickerItemBuilder, String> {

  @NotNull
  UIIconPickerItemBuilder setActionHandler(@Nullable UIActionHandler action);
}
