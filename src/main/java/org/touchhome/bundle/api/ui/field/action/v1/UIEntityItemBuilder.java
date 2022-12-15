package org.touchhome.bundle.api.ui.field.action.v1;

import org.jetbrains.annotations.NotNull;

public interface UIEntityItemBuilder<Owner, Value> extends UIEntityBuilder {

    String getSeparatedText();

    Owner setSeparatedText(String text);

    String getStyle();

    Owner setValue(Value value);

    Owner setTitle(String title);

    Owner setDisabled(boolean disabled);

    Owner setDescription(String description);

    Owner setOrder(int order);

    default Owner setIcon(String icon) {
        return setIcon(icon, null);
    }

    Owner setIcon(String icon, String iconColor);

    Owner setColor(String color);

    Owner appendStyle(@NotNull String style, @NotNull String value);

    Owner setOuterClass(String outerClass);

    Owner addFetchValueHandler(String key, Runnable fetchValueHandler);
}
