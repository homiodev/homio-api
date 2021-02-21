package org.touchhome.bundle.api.ui.field;

public enum UIFieldType {
    Slider,

    IpAddress,
    Password, // shows *** for users without admin rights

    Selection,
    TextSelectBoxDynamic, // text input type with ability to select values from server

    Float,
    Duration,
    StaticDate,

    String,
    Boolean,
    Integer, // for integer we may set metadata as min, max
    Color,
    Json,

    Chips, // https://material.angular.io/components/chips/examples

    // special type (default for detect field type by java type)
    AutoDetect
}
