package org.touchhome.bundle.api.ui.field.action.v1.item;

import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

import java.util.Collection;

public interface UISelectBoxItemBuilder extends UIEntityItemBuilder<UISelectBoxItemBuilder, String> {

    boolean isAsButton();

    UISelectBoxItemBuilder setAsButton(boolean asButton);

    UISelectBoxItemBuilder setOptions(Collection<OptionModel> options);

    UISelectBoxItemBuilder addOption(OptionModel option);

    default UISelectBoxItemBuilder setSelected(String selected) {
        setValue(selected);
        return this;
    }

    // uses if want replace dimmer with select box from min:
    UISelectBoxItemBuilder setSelectReplacer(int min, int max, String selectReplacer);

    String getSelectReplacer();

    Collection<OptionModel> getOptions();
}
