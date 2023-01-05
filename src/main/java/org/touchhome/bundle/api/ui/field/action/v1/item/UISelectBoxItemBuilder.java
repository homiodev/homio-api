package org.touchhome.bundle.api.ui.field.action.v1.item;

import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

import java.util.Collection;

public interface UISelectBoxItemBuilder extends UIEntityItemBuilder<UISelectBoxItemBuilder, String> {

    boolean isAsButton();

    /**
     * icon or text should be not null
     */
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
