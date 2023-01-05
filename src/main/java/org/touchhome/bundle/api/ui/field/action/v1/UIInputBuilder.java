package org.touchhome.bundle.api.ui.field.action.v1;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.ui.action.UIActionHandler;
import org.touchhome.bundle.api.ui.field.action.v1.item.UIButtonItemBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.layout.UILayoutBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.layout.dialog.UIDialogLayoutBuilder;

import java.util.Collection;
import java.util.function.Consumer;

public interface UIInputBuilder extends UILayoutBuilder {

    void from(@NotNull UIInputBuilder source);

    @Unmodifiable
    @NotNull Collection<UIInputEntity> buildAll();

    @NotNull EntityContext getEntityContext();

    void fireFetchValues();

    @Nullable UIActionHandler findActionHandler(@NotNull String key);

    @NotNull default UIButtonItemBuilder addSelectableButton(@NotNull String text, @NotNull UIActionHandler action) {
        return addSelectableButton(text, null, null, action);
    }

    @NotNull default UIButtonItemBuilder addSelectableButton(@NotNull String name, @Nullable String icon, @Nullable String iconColor,
                                                    @NotNull UIActionHandler action) {
        return addSelectableButton(name, icon, iconColor, action, getNextOrder());
    }

    @NotNull UIButtonItemBuilder addSelectableButton(@NotNull String name, @Nullable String icon, @Nullable String iconColor,
                                            @NotNull UIActionHandler action, int order);

    @NotNull default DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(@NotNull String name, @Nullable String icon,
                                                                            @Nullable String color, @Nullable Integer dialogWidth,
                                                                            @NotNull UIActionHandler action) {
        return addOpenDialogSelectableButton(name, icon, color, dialogWidth, action, getNextOrder());
    }

    @NotNull DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(@NotNull String name, @Nullable String icon,
                                                                    @Nullable String color, @Nullable Integer dialogWidth,
                                                                    @NotNull UIActionHandler action, int order);

    @NotNull default DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(@NotNull String name, @Nullable Integer dialogWidth,
                                                                            @NotNull UIActionHandler action) {
        return addOpenDialogSelectableButton(name, null, null, dialogWidth, action);
    }

    @NotNull default DialogEntity<UIButtonItemBuilder> addOpenDialogSelectableButton(@NotNull String name, @Nullable Integer dialogWidth,
                                                                            @NotNull UIActionHandler action, int order) {
        return addOpenDialogSelectableButton(name, null, null, dialogWidth, action, order);
    }

    interface DialogEntity<T> {
        @NotNull UIInputBuilder up();

        @NotNull UIInputBuilder edit(Consumer<T> editHandler);

        @NotNull UIInputBuilder editDialog(Consumer<UIDialogLayoutBuilder> editDialogHandler);
    }
}
