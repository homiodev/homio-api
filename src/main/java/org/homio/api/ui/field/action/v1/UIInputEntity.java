package org.homio.api.ui.field.action.v1;

import org.homio.api.ui.UIActionHandler;

public interface UIInputEntity {

    String getEntityID();

    String getTitle();

    String getItemType();

    int getOrder();

    default UIActionHandler findAction(String actionEntityID) {
        if (getEntityID().equals(actionEntityID) && this instanceof UIActionHandler) {
            return (UIActionHandler) this;
        }
        return null;
    }
}
