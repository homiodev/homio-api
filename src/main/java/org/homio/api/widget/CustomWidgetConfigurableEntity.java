package org.homio.api.widget;

import org.homio.api.entity.HasJsonData;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.ui.field.action.HasDynamicContextMenuActions;
import org.homio.api.ui.field.action.HasDynamicUIFields;
import org.jetbrains.annotations.NotNull;

/**
 * Implement by entities that able to create CustomWidgetEntity
 */
public interface CustomWidgetConfigurableEntity extends
        HasDynamicContextMenuActions, HasEntityIdentifier {

    void assembleUIFields(@NotNull HasDynamicUIFields.UIFieldBuilder uiFieldBuilder,
                          @NotNull HasJsonData sourceEntity);
}