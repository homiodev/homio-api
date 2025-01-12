package org.homio.api.ui.field.action.v1;

import org.homio.api.Context;
import org.homio.api.model.Icon;
import org.homio.api.ui.UIActionHandler;
import org.homio.api.ui.field.action.v1.item.UIButtonItemBuilder;
import org.homio.api.ui.field.action.v1.layout.UILayoutBuilder;
import org.homio.api.ui.field.action.v1.layout.dialog.UIDialogLayoutBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.function.Consumer;

public interface UIInputBuilder extends UILayoutBuilder {

  void from(@Nullable UIInputBuilder source);

  @Unmodifiable
  @NotNull Collection<UIInputEntity> buildAll();

  @NotNull
  Context context();

  void fireFetchValues();

  @Nullable UIActionHandler findActionHandler(@NotNull String key);

  default @NotNull UIButtonItemBuilder addSelectableButton(@NotNull String text, @Nullable UIActionHandler action) {
    return addSelectableButton(text, null, action);
  }

  default @NotNull UIButtonItemBuilder addSelectableButton(@NotNull String name, @Nullable Icon icon, @Nullable UIActionHandler action) {
    return addSelectableButton(name, icon, action, getNextOrder());
  }

  @NotNull UIButtonItemBuilder addSelectableButton(@NotNull String name, @Nullable Icon icon,
                                                   @Nullable UIActionHandler action, int order);

  default @NotNull DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(@NotNull String name, @Nullable Icon icon,
                                                                                   @Nullable Integer dialogWidth,
                                                                                   @NotNull UIActionHandler action) {
    return addOpenDialogSelectableButton(name, icon, dialogWidth, action, getNextOrder());
  }

  @NotNull DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(@NotNull String name, @Nullable Icon icon,
                                                                           @Nullable Integer dialogWidth,
                                                                           @NotNull UIActionHandler action, int order);

  void addOpenDialogSelectableButtonFromClass(@NotNull String name,
                                              @Nullable Icon icon,
                                              @NotNull Class<?> entityClass,
                                              @NotNull UIActionHandler action);

  default @NotNull DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(@NotNull String name, @Nullable Integer dialogWidth,
                                                                                   @NotNull UIActionHandler action) {
    return addOpenDialogSelectableButton(name, null, dialogWidth, action);
  }

  default @NotNull DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(@NotNull String name, @Nullable Integer dialogWidth,
                                                                                   @NotNull UIActionHandler action, int order) {
    return addOpenDialogSelectableButton(name, null, dialogWidth, action, order);
  }

  interface DialogEntity<T> {

    @NotNull UIInputBuilder up();

    @NotNull UIInputBuilder edit(Consumer<T> editHandler);

    @NotNull UIInputBuilder editDialog(Consumer<UIDialogLayoutBuilder> editDialogHandler);
  }
}
