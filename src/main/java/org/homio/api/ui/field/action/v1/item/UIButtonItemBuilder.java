package org.homio.api.ui.field.action.v1.item;

import org.homio.api.model.Icon;
import org.homio.api.ui.field.action.v1.UIEntityItemBuilder;
import org.jetbrains.annotations.NotNull;

public interface UIButtonItemBuilder extends UIEntityItemBuilder<UIButtonItemBuilder, String> {

  UIButtonItemBuilder setText(@NotNull String text);

  // default - 32
  UIButtonItemBuilder setHeight(int height);

  UIButtonItemBuilder setPrimary(boolean primary);

  UIButtonItemBuilder setConfirmMessage(String message);

  UIButtonItemBuilder setConfirmMessageDialogColor(String color);

  UIButtonItemBuilder setConfirmMessageDialogTitle(String title);

  UIButtonItemBuilder setConfirmMessageDialogIcon(Icon icon);
}
