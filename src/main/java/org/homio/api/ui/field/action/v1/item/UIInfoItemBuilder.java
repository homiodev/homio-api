package org.homio.api.ui.field.action.v1.item;

import org.homio.api.entity.BaseEntity;
import org.homio.api.ui.field.action.v1.UIEntityItemBuilder;
import org.jetbrains.annotations.Nullable;

public interface UIInfoItemBuilder extends UIEntityItemBuilder<UIInfoItemBuilder, String> {

    InfoType getInfoType();

    /**
     * Set text clickable and navigate to entity
     */
    UIInfoItemBuilder linkToEntity(@Nullable BaseEntity entity);

    enum InfoType {
        Text, HTML, Markdown
    }
}
