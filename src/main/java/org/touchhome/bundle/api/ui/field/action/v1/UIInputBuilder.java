package org.touchhome.bundle.api.ui.field.action.v1;

import org.jetbrains.annotations.Unmodifiable;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.ui.action.UIActionHandler;
import org.touchhome.bundle.api.ui.field.action.v1.item.UISelectableButtonItemBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.layout.UILayoutBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.layout.dialog.UIDialogLayoutBuilder;

import java.util.Collection;
import java.util.function.Consumer;

public interface UIInputBuilder extends UILayoutBuilder {
    @Unmodifiable
    Collection<UIInputEntity> build();

    EntityContext getEntityContext();

    void fireFetchValues();

    UIActionHandler findActionHandler(String key);

    UISelectableButtonItemBuilder addSelectableButton(String name, UIActionHandler action);

    DialogEntity<UISelectableButtonItemBuilder> addOpenDialogSelectableButton(String name, String icon, String color, UIActionHandler action);

    default DialogEntity<UISelectableButtonItemBuilder> addOpenDialogSelectableButton(String name, UIActionHandler action) {
        return addOpenDialogSelectableButton(name, null, null, action);
    }

    interface DialogEntity<T> {
        UIInputBuilder up();

        UIInputBuilder edit(Consumer<T> editHandler);

        UIInputBuilder editDialog(Consumer<UIDialogLayoutBuilder> editDialogHandler);
    }
}
