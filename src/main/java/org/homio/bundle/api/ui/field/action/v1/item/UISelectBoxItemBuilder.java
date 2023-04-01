package org.homio.bundle.api.ui.field.action.v1.item;

import java.util.Collection;
import org.homio.bundle.api.model.OptionModel;
import org.homio.bundle.api.ui.field.action.v1.UIEntityItemBuilder;
import org.jetbrains.annotations.Nullable;

public interface UISelectBoxItemBuilder extends UIEntityItemBuilder<UISelectBoxItemBuilder, String> {

    boolean isAsButton();

    UISelectBoxItemBuilder setAsButton(@Nullable String icon, @Nullable String iconColor, @Nullable String text);

    UISelectBoxItemBuilder addOption(OptionModel option);

    default UISelectBoxItemBuilder addOptions(Collection<OptionModel> options) {
        for (OptionModel option : options) {
            addOption(option);
        }
        return this;
    }

    default UISelectBoxItemBuilder setSelected(String selected) {
        setValue(selected);
        return this;
    }

    // uses if want replace dimmer with select box from min:
    UISelectBoxItemBuilder setSelectReplacer(int min, int max, String selectReplacer);

    String getSelectReplacer();

    Collection<OptionModel> getOptions();

    UISelectBoxItemBuilder setOptions(Collection<OptionModel> options);

    UISelectBoxItemBuilder setPlaceholder(String placeholder);
}
