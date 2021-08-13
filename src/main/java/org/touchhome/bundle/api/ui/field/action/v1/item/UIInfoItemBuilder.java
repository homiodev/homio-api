package org.touchhome.bundle.api.ui.field.action.v1.item;

import org.touchhome.bundle.api.ui.field.action.v1.UIEntityItemBuilder;

public interface UIInfoItemBuilder extends UIEntityItemBuilder<UIInfoItemBuilder, String> {

    enum InfoType {
        Text, HTML, Markdown
    }
}
