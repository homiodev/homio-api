package org.touchhome.bundle.api.ui.field.selection.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface SelectionWithDynamicParameterFields {
    DynamicParameterFields getDynamicParameterFields(RequestDynamicParameter request);

    @Getter
    @AllArgsConstructor
    class RequestDynamicParameter {
        Object selectionHolder;
        DynamicRequestType dynamicRequestType;
    }
}
