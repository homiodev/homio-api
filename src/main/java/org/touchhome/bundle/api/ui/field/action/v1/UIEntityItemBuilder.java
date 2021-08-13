package org.touchhome.bundle.api.ui.field.action.v1;

public interface UIEntityItemBuilder<Owner, Value> extends UIEntityBuilder {

    Owner setValue(Value value);

    Owner setDisabled(boolean disabled);

    Owner setOrder(int order);

    default Owner setIcon(String icon) {
        return setIcon(icon, null);
    }

    Owner setIcon(String icon, String iconColor);

    Owner setColor(String color);

    Owner setStyle(String style);

    Owner addFetchValueHandler(String key, Runnable fetchValueHandler);
}
