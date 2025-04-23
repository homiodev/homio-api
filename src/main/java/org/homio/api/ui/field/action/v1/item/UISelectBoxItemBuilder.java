package org.homio.api.ui.field.action.v1.item;

import java.util.Collection;
import org.homio.api.entity.BaseEntity;
import org.homio.api.model.Icon;
import org.homio.api.model.OptionModel;
import org.homio.api.ui.field.action.v1.UIEntityItemBuilder;
import org.homio.api.ui.field.selection.dynamic.DynamicOptionLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UISelectBoxItemBuilder extends UIEntityItemBuilder<UISelectBoxItemBuilder, String> {

    boolean isAsButton();

    @NotNull UISelectBoxItemBuilder setRequired(boolean value);

    @NotNull UISelectBoxItemBuilder setMultiSelect(boolean value);

    @NotNull UIButtonItemBuilder setAsButton(@Nullable Icon icon, @Nullable String text);

    // default - true
    @NotNull UISelectBoxItemBuilder setHighlightSelected(boolean value);

    @NotNull UISelectBoxItemBuilder setLazyVariableGroup();

    @NotNull UISelectBoxItemBuilder setLazyVariable();

    @NotNull UISelectBoxItemBuilder setLazyItemOptions(@NotNull Class<? extends BaseEntity> itemClass);

    @NotNull UISelectBoxItemBuilder setLazyOptionLoader(@NotNull Class<? extends DynamicOptionLoader> itemClass);

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
