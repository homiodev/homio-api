package org.homio.api.ui.field.action.v1.item;

import org.homio.api.ui.UIActionHandler;
import org.homio.api.ui.field.action.v1.UIEntityItemBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UIColorPickerItemBuilder extends UIEntityItemBuilder<UIColorPickerItemBuilder, String> {

  @NotNull
  UIColorPickerItemBuilder setActionHandler(@Nullable UIActionHandler action);

  // default is ColorPicker
  @NotNull
  UIColorPickerItemBuilder setColorType(@NotNull ColorType colorType);

  enum ColorType {
    ColorPicker, ColorSlider
  }
}