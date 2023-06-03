package org.homio.api.ui.field.action.v1.item;

import java.util.Collection;
import org.homio.api.model.Icon;
import org.homio.api.model.OptionModel;
import org.homio.api.ui.field.action.v1.UIEntityItemBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UISelectBoxItemBuilder extends UIEntityItemBuilder<UISelectBoxItemBuilder, String> {

    boolean isAsButton();

    @NotNull UIButtonItemBuilder setAsButton(@Nullable Icon icon, @Nullable String text);

    // default - true
    @NotNull UISelectBoxItemBuilder setHighlightSelected(boolean value);

    @NotNull UISelectBoxItemBuilder addOption(@NotNull OptionModel option);

    default @NotNull UISelectBoxItemBuilder addOptions(@NotNull Collection<OptionModel> options) {
        for (OptionModel option : options) {
            addOption(option);
        }
        return this;
    }

    default @NotNull UISelectBoxItemBuilder setSelected(@NotNull String selected) {
        setValue(selected);
        return this;
    }

    // uses if want replace dimmer with select box from min:
    @NotNull UISelectBoxItemBuilder setSelectReplacer(int min, int max, @Nullable String selectReplacer);

    @Nullable String getSelectReplacer();

    @NotNull Collection<OptionModel> getOptions();

    @NotNull UISelectBoxItemBuilder setOptions(@NotNull Collection<OptionModel> options);

    @NotNull UISelectBoxItemBuilder setPlaceholder(@Nullable String placeholder);
}
