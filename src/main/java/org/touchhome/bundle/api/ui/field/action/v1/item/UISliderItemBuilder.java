package org.touchhome.bundle.api.ui.field.action.v1.item;

import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

public interface UISliderItemBuilder extends UIEntityItemBuilder<UISliderItemBuilder, Float> {
    Float getMin();

    Float getMax();

    Float getStep();

    UISliderItemBuilder setStep(Float step);

    boolean isHideThumbLabel();

    UISliderItemBuilder setHideThumbLabel(boolean hideThumbLabel);

    boolean isRequired();

    boolean isAllowEraseValue();

    UISliderItemBuilder setAllowEraseValue(boolean value);

    UISliderItemBuilder setRequired(boolean required);

    SliderType getSliderType();

    UISliderItemBuilder setSliderType(SliderType sliderType);

    enum SliderType {
        Regular, Input
    }
}
