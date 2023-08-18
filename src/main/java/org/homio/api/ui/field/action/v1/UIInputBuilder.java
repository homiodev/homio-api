package org.homio.api.ui.field.action.v1;

import org.homio.api.EntityContext;
import org.homio.api.model.Icon;
import org.homio.api.ui.action.UIActionHandler;
import org.homio.api.ui.field.action.v1.item.UIButtonItemBuilder;
import org.homio.api.ui.field.action.v1.layout.UILayoutBuilder;
import org.homio.api.ui.field.action.v1.layout.dialog.UIDialogLayoutBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.function.Consumer;

public interface UIInputBuilder extends UILayoutBuilder {

    void from(@NotNull UIInputBuilder source);

    @Unmodifiable
    @NotNull Collection<UIInputEntity> buildAll();

    @NotNull
    EntityContext getEntityContext();

    void fireFetchValues();

    @Nullable UIActionHandler findActionHandler(@NotNull String key);

    @NotNull
    default UIButtonItemBuilder addSelectableButton(@NotNull String text, @Nullable UIActionHandler action) {
        return addSelectableButton(text, null, action);
    }

    @NotNull
    default UIButtonItemBuilder addSelectableButton(@NotNull String name, @Nullable Icon icon, @Nullable UIActionHandler action) {
        return addSelectableButton(name, icon, action, getNextOrder());
    }

    @NotNull UIButtonItemBuilder addSelectableButton(@NotNull String name, @Nullable Icon icon,
                                                     @Nullable UIActionHandler action, int order);

    @NotNull
    default DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(@NotNull String name, @Nullable Icon icon,
                                                                            @Nullable Integer dialogWidth,
                                                                            @NotNull UIActionHandler action) {
        return addOpenDialogSelectableButton(name, icon, dialogWidth, action, getNextOrder());
    }

    @NotNull DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(@NotNull String name, @Nullable Icon icon,
                                                                             @Nullable Integer dialogWidth,
                                                                             @NotNull UIActionHandler action, int order);

    @NotNull
    default DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(@NotNull String name, @Nullable Integer dialogWidth,
                                                                            @NotNull UIActionHandler action) {
        return addOpenDialogSelectableButton(name, null, dialogWidth, action);
    }

    @NotNull
    default DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(@NotNull String name, @Nullable Integer dialogWidth,
                                                                            @NotNull UIActionHandler action, int order) {
        return addOpenDialogSelectableButton(name, null, dialogWidth, action, order);
    }

    interface DialogEntity<T> {

        @NotNull UIInputBuilder up();

        @NotNull UIInputBuilder edit(Consumer<T> editHandler);

        @NotNull UIInputBuilder editDialog(Consumer<UIDialogLayoutBuilder> editDialogHandler);
    }
}
