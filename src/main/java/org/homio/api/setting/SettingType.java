package org.homio.api.setting;

import lombok.Getter;

@Getter
public enum SettingType {
    // Description type uses for showing text inside setting panel on whole width
    Description(false),
    SelectBoxButton,
    SelectBox,

    // Button that fires server action
    Button(false),
    // Store as boolean true/false
    Toggle,
    Upload,
    TextInput,
    /**
     * return type must be enum. Handle as buttons instead of select box
     */
    EnumButtons,
    ColorPicker,
    Chips,

    // Slider with min/max/step parameters
    Slider,

    Float,
    Boolean,
    Integer;

    private final boolean storable;

    SettingType() {
        this.storable = true;
    }

    SettingType(boolean storable) {
        this.storable = storable;
    }
}
