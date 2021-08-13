package org.touchhome.bundle.api.ui.field.action.v1.layout;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.touchhome.bundle.api.ui.action.UIActionHandler;
import org.touchhome.bundle.api.ui.field.action.v1.UIEntityBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.item.*;
import org.touchhome.bundle.api.ui.field.action.v1.layout.dialog.UIDialogLayoutBuilder;
import org.touchhome.bundle.api.ui.field.action.v1.layout.dialog.UIStickyDialogItemBuilder;

import java.util.Collection;

public interface UILayoutBuilder extends UIEntityBuilder {
    @Unmodifiable
    Collection<UIEntityBuilder> getUiEntityBuilders();

    @Unmodifiable
    Collection<UIEntityItemBuilder> getUiEntityItemBuilders();

    void addRawUIEntityBuilder(@NotNull String name, UIEntityBuilder source);

    UIFlexLayoutBuilder addFlex(@NotNull String name);

    UIInfoItemBuilder addInfo(@NotNull String name, UIInfoItemBuilder.InfoType infoType);

    default UIInfoItemBuilder addInfo(@NotNull String name) {
        return addInfo(name, UIInfoItemBuilder.InfoType.Text);
    }

    UISelectBoxItemBuilder addSelectBox(@NotNull String name, UIActionHandler action);

    UICheckboxItemBuilder addCheckbox(@NotNull String name, boolean value, UIActionHandler action);

    UIMultiButtonItemBuilder addMultiButton(String name, UIActionHandler action);

    UISliderItemBuilder addSlider(@NotNull String name, int min, int max, UIActionHandler action);

    UIButtonItemBuilder addButton(@NotNull String name, UIActionHandler action);

    UIStickyDialogItemBuilder addStickyDialogButton(@NotNull String name, String icon, String iconColor);

    UIDialogLayoutBuilder addOpenDialogActionButton(@NotNull String name, String icon, String iconColor);
}
