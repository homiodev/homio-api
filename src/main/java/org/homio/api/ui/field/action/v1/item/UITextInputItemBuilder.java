package org.homio.api.ui.field.action.v1.item;

import org.homio.api.ui.UIActionHandler;
import org.homio.api.ui.field.action.v1.UIEntityItemBuilder;
import org.jetbrains.annotations.NotNull;

public interface UITextInputItemBuilder
    extends UIEntityItemBuilder<UITextInputItemBuilder, String> {

  /**
   * Add apply button to input field. Default - false
   *
   * @param value on/off
   * @return this
   */
  @NotNull
  UITextInputItemBuilder setRequireApply(boolean value);

  @NotNull
  UITextInputItemBuilder setRequired(boolean value);

  @NotNull
  UITextInputItemBuilder setActionHandler(@NotNull UIActionHandler handler);

  enum InputType {
    Text,
    TextArea,
    Password,
    JSON,
    Ip
  }
}
