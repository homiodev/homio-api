package org.touchhome.bundle.api.state;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ButtonType extends State {
    private final ButtonPressType buttonPressType;

    @Override
    public float floatValue() {
        throw new IllegalStateException("Unable to fetch float value from button");
    }

    @Override
    public int intValue() {
        throw new IllegalStateException("Unable to fetch int value from button");
    }

    @Override
    public boolean boolValue() {
        throw new IllegalStateException("Unable to fetch boolean value from button");
    }

    @Override
    public String stringValue() {
        return buttonPressType.parameterValue;
    }

    @Override
    public String toString() {
        return "ButtonState{buttonPressType=" + buttonPressType + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ButtonType that = (ButtonType) o;

        return buttonPressType == that.buttonPressType;
    }

    @Override
    public int hashCode() {
        return buttonPressType.hashCode();
    }

    public enum ButtonPressType {
        SHORT_PRESS("shortpress"),
        DOUBLE_PRESS("doublepress"),
        LONG_PRESS("longpress");

        private final String parameterValue;

        ButtonPressType(String parameterValue) {
            this.parameterValue = parameterValue;
        }

        @Override
        public String toString() {
            return parameterValue;
        }
    }
}
