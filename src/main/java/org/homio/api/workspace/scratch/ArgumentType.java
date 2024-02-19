package org.homio.api.workspace.scratch;

public enum ArgumentType {
    /**
     * Boolean value with hexagonal placeholder
     */
    Boolean,

    /**
     * Numeric value with color picker
     */
    color,

    /**
     * Numeric value with text field
     */
    number,

    /**
     * String value with text field
     */
    string,

    reference,

    variable,

    /**
     * Boolean checkbox UI representation
     */
    checkbox,

    icon,

    broadcast,

    setting,

    timer,

    calendar
}
