package org.touchhome.bundle.api.ui.field.action.v1.item;

import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

public interface UISliderItemBuilder extends UIEntityItemBuilder<UISliderItemBuilder, Float> {
    Float getMin();

    Float getMax();

    Float getStep();

    boolean isHideThumbLabel();

    boolean isRequired();

    SliderType getSliderType();

    UISliderItemBuilder setSliderType(SliderType sliderType);

    UISliderItemBuilder setStep(Float step);

    UISliderItemBuilder setRequired(boolean required);

    UISliderItemBuilder setHideThumbLabel(boolean hideThumbLabel);

    enum SliderType {
        Regular, Input
    }
}
