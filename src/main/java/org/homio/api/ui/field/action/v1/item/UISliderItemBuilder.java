package org.homio.api.ui.field.action.v1.item;

import org.homio.api.ui.field.action.v1.UIEntityItemBuilder;

public interface UISliderItemBuilder extends UIEntityItemBuilder<UISliderItemBuilder, Float> {
    Float getMin();

    Float getMax();

    Float getStep();

    UISliderItemBuilder setStep(Float step);

    boolean isHideThumbLabel();

    UISliderItemBuilder setHideThumbLabel(boolean hideThumbLabel);

    boolean isRequired();

    UISliderItemBuilder setRequired(boolean required);

    SliderType getSliderType();

    UISliderItemBuilder setSliderType(SliderType sliderType);

    enum SliderType {
        Regular, Input
    }
}
