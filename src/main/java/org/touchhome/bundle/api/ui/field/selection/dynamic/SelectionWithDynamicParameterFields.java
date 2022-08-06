package org.touchhome.bundle.api.ui.field.selection.dynamic;

public interface SelectionWithDynamicParameterFields {
    DynamicParameterFields getDynamicParameterFields(Object selectionHolder, DynamicRequestType dynamicRequestType);
}
