package org.touchhome.bundle.api.ui.field.action.v1.item;

import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

import java.util.Collection;

public interface UISelectBoxItemBuilder extends UIEntityItemBuilder<UISelectBoxItemBuilder, String> {

    UISelectBoxItemBuilder setOptions(Collection<OptionModel> options);

    UISelectBoxItemBuilder addOption(OptionModel option);

    default UISelectBoxItemBuilder setSelected(String selected) {
        setValue(selected);
        return this;
    }
}
