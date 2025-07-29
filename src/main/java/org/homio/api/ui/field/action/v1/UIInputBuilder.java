package org.homio.api.ui.field.action.v1;

import java.util.Collection;
import java.util.function.Consumer;
import org.homio.api.Context;
import org.homio.api.model.Icon;
import org.homio.api.ui.UIActionHandler;
import org.homio.api.ui.field.action.v1.item.UIButtonItemBuilder;
import org.homio.api.ui.field.action.v1.layout.UILayoutBuilder;
import org.homio.api.ui.field.action.v1.layout.dialog.UIDialogLayoutBuilder;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface UIInputBuilder extends UILayoutBuilder {

  void from(@Nullable UIInputBuilder source);

  @Unmodifiable
  @NotNull
  Collection<UIInputEntity> buildAll();

  @NotNull
  Context context();

  void fireFetchValues();

  @Nullable
  UIActionHandler findActionHandler(@NotNull String key);

  default @NotNull UIButtonItemBuilder addSelectableButton(
      @NotNull String text, @Nullable UIActionHandler action) {
    return addSelectableButton(text, null, action);
  }

  default @NotNull UIButtonItemBuilder addSelectableButton(
      @NotNull String name, @Nullable Icon icon, @Nullable UIActionHandler action) {
    return addSelectableButton(name, icon, action, getNextOrder());
  }

  @NotNull
  UIButtonItemBuilder addSelectableButton(
      @NotNull String name, @Nullable Icon icon, @Nullable UIActionHandler action, int order);

  default @NotNull DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(
      @NotNull String name, @Nullable Icon icon, @NotNull UIActionHandler action) {
    return addOpenDialogSelectableButton(name, icon, action, getNextOrder());
  }

  @NotNull
  DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(
      @NotNull String name, @Nullable Icon icon, @NotNull UIActionHandler action, int order);

  default void addOpenDialogSelectableButtonFromClass(
      @NotNull String name,
      @Nullable Icon icon,
      @NotNull Class<?> entityClass,
      @NotNull UIActionHandler action) {
    addOpenDialogSelectableButtonFromClassInstance(
        name, icon, CommonUtils.newInstance(entityClass), action);
  }

  void addOpenDialogSelectableButtonFromClassInstance(
      @NotNull String name,
      @Nullable Icon icon,
      @NotNull Object entityInstance,
      @NotNull UIActionHandler action);

  default @NotNull DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(
      @NotNull String name, @NotNull UIActionHandler action) {
    return addOpenDialogSelectableButton(name, null, action);
  }

  default @NotNull DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(
      @NotNull String name, @NotNull UIActionHandler action, int order) {
    return addOpenDialogSelectableButton(name, null, action, order);
  }

  interface DialogEntity<T> {

    @NotNull
    UIInputBuilder up();

    @NotNull
    DialogEntity<T> dialogWidth(int dialogWidth);

    @NotNull
    UIInputBuilder edit(Consumer<T> editHandler);

    @NotNull
    UIInputBuilder editDialog(Consumer<UIDialogLayoutBuilder> editDialogHandler);
  }
}
