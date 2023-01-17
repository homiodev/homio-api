package org.touchhome.bundle.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Uses for styling html input element position */
@Getter
@RequiredArgsConstructor
public enum StylePosition implements KeyValueEnum {
    None("None"),
    TopLeft("Top Left"),
    TopRight("Top Right"),
    BottomLeft("Bottom Left"),
    BottomRight("Bottom Right"),
    BottomCenter("Bottom Center"),
    TopCenter("Top Center"),
    MiddleCenter("Middle Center"),
    MiddleLeft("Middle Left"),
    MiddleRight("Middle Right");

    private final String textValue;

    @Override
    public String getValue() {
        return textValue;
    }
}
