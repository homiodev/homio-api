package org.touchhome.bundle.api.ui.field.selection.dynamic;

// distinguish property if one entity i.e.(MqttBaseEntity) has few option dynamic classes for slider
// or chart widgets
public enum DynamicRequestType {
    // fetch status for all widget types to get single raw value
    GetValue,
    // set value for push/slider/toggle widget types to set single raw value
    SetValue,
    // other widgets
    TimeSeries,
    // other widgets
    Default
}
