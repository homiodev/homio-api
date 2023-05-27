package org.homio.api.ui.field.selection.dynamic;

/**
 * Marker interface for dynamic inner fields
 */
public interface DynamicParameterFields {

    default String getGroupName() {
        return null;
    }

    default String getBorderColor() {
        return null;
    }
}
