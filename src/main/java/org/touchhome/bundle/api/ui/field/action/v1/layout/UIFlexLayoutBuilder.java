package org.touchhome.bundle.api.ui.field.action.v1.layout;

import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.ui.UI;

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
            setTitle(getTitle(), borderColor);
        }
        return this;
    }

    default UIFlexLayoutBuilder setBorderArea(String title) {
        appendStyle("border", "1px solid " + UI.Color.PRIMARY_COLOR);
        appendStyle("border-radius", "3px");
        appendStyle("margin", "3px");
        appendStyle("padding", "5px");

        setTitle(title);
        return this;
    }

    default UIFlexLayoutBuilder setTitle(String title) {
        return setTitle(title, null);
    }

    UIFlexLayoutBuilder setTitle(String title, @Nullable String color);

    String getTitle();

    default UIFlexLayoutBuilder setBackgroundColor(String backgroundColor) {
        appendStyle("background", backgroundColor);
        return this;
    }
}
