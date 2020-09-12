package org.touchhome.bundle.api.ui.field;

public enum UIFieldType {
    Slider,

    Selection,
    TextSelectBoxDynamic, // text input type with ability to select values from server

    Float,
    Duration,
    StaticDate,
    Image,

    String,
    Boolean,
    Integer, // for integer we may set metadata as min, max
    Color,
    Json,

    // special type (default for detect field type by java type)
    AutoDetect
}
