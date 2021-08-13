package org.touchhome.bundle.api.ui.field.action.v1.item;

import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

public interface UISliderItemBuilder extends UIEntityItemBuilder<UISliderItemBuilder, Integer> {

    // uses if want replace dimmer with select box from min:
    UISliderItemBuilder setSelectReplacer(String selectReplacer);
}
