package org.touchhome.bundle.api.ui.field.selection.dynamic;

/**
 * Marker interface for dynamic inner fields
 */
public interface DynamicParameterFields {

    /**
     * Specify field where to save dynamic parameters
     */
    default String getHolderField() {
        return "dynamicParameterFieldsHolder";
    }

    default String getGroupName() {
        return null;
    }

    default String getBorderColor() {
        return null;
    }
}
