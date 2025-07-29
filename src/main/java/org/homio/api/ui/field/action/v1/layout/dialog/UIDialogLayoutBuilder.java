package org.homio.api.ui.field.action.v1.layout.dialog;

import java.util.function.Consumer;
import org.homio.api.model.Icon;
import org.homio.api.ui.field.action.v1.UIEntityBuilder;
import org.homio.api.ui.field.action.v1.layout.UIFlexLayoutBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UIDialogLayoutBuilder extends UIEntityBuilder {

  DialogEntity<UIFlexLayoutBuilder> addFlex(@NotNull String name);

  default DialogEntity<UIFlexLayoutBuilder> addFlex(
      @NotNull String name, Consumer<UIFlexLayoutBuilder> flexConsumer) {
    DialogEntity<UIFlexLayoutBuilder> flex = addFlex(name);
    flex.edit(flexConsumer);
    return flex;
  }

  default UIDialogLayoutBuilder setBackgroundColor(@NotNull String backgroundColor) {
    appendStyle("background", backgroundColor);
    return this;
  }

  String getStyle();

  UIDialogLayoutBuilder appendStyle(@NotNull String style, @NotNull String value);

  UIDialogLayoutBuilder setTitle(@NotNull String title, @Nullable Icon icon);

  default UIDialogLayoutBuilder setTitle(@NotNull String title) {
    return setTitle(title, null);
  }

  interface DialogEntity<T> {

    UIDialogLayoutBuilder up();

    UIDialogLayoutBuilder edit(Consumer<T> editHandler);
  }
}
