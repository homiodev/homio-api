package org.touchhome.bundle.api.ui.field.action.v1.layout;

import org.jetbrains.annotations.Nullable;

import static org.touchhome.bundle.api.util.TouchHomeUtils.PRIMARY_COLOR;

public interface UIFlexLayoutBuilder extends UILayoutBuilder {

    default UIFlexLayoutBuilder columnFlexDirection() {
        return columnFlexDirection(true);
    }

    default UIFlexLayoutBuilder columnFlexDirection(boolean columnDirection) {
        appendStyle("flex-direction", columnDirection ? "column" : "row");
        return this;
    }

    default UIFlexLayoutBuilder setBorderColor(@Nullable String borderColor) {
        if (borderColor == null) {
            removeStyle("border");
        } else {
            appendStyle("border", "1px solid " + borderColor);
        }
        return this;
    }

    default UIFlexLayoutBuilder setBorderArea(String title) {
        appendStyle("border", "1px solid " + PRIMARY_COLOR);
        appendStyle("border-radius", "3px");
        appendStyle("margin", "3px");
        appendStyle("padding", "5px");

        setTitle(title);
        return this;
    }

    UIFlexLayoutBuilder setTitle(String title);

    default UIFlexLayoutBuilder setBackgroundColor(String backgroundColor) {
        appendStyle("background", backgroundColor);
        return this;
    }
}
