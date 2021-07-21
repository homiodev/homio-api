package org.touchhome.bundle.api.ui.field.action;

import org.touchhome.bundle.api.ui.field.action.v1.UIInputBuilder;

/**
 * For BaseItems that wants dynamic context menu items
 */
public interface HasDynamicContextMenuActions {
    void assembleActions(UIInputBuilder uiInputBuilder);
}
