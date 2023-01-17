package org.touchhome.bundle.api.ui.field.action.v1.item;

import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

public interface UIInfoItemBuilder extends UIEntityItemBuilder<UIInfoItemBuilder, String> {

    InfoType getInfoType();

    enum InfoType {
        Text,
        HTML,
        Markdown
    }
}
