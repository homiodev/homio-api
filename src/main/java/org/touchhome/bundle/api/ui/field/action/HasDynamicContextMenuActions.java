package org.touchhome.bundle.api.ui.field.action;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.ui.field.action.impl.DynamicContextMenuAction;

import java.util.Set;

/**
 * For BaseItems that wants dynamic context menu items
 */
public interface HasDynamicContextMenuActions {
    Set<DynamicContextMenuAction> getActions(EntityContext entityContext);
}
