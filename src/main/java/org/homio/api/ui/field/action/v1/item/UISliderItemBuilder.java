package org.homio.api.ui.field.action.v1.item;

import org.homio.api.ui.field.action.v1.UIEntityItemBuilder;

public interface UISliderItemBuilder extends UIEntityItemBuilder<UISliderItemBuilder, Float> {

  Float getMin();

  Float getMax();

  Float getStep();

  UISliderItemBuilder setStep(Float step);

  UISliderItemBuilder setThumbLabel(String label);

  boolean isHideThumbLabel();

  UISliderItemBuilder setHideThumbLabel(boolean hideThumbLabel);

  UISliderItemBuilder setRequired(boolean required);

  SliderType getSliderType();

  UISliderItemBuilder setSliderType(SliderType sliderType);

  UISliderItemBuilder setDefaultValue(Float defaultValue);

  enum SliderType {
    Regular,
    Input
  }
}
