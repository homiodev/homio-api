package org.homio.api.ui.field.action;

import org.homio.api.EntityContext;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.ui.action.UIActionHandler;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.json.JSONObject;

/**
 * For BaseItems that wants dynamic context menu items
 */
public interface HasDynamicContextMenuActions {

    void assembleActions(UIInputBuilder uiInputBuilder);

    default ActionResponseModel handleAction(EntityContext entityContext, String actionID, JSONObject params) throws Exception {
        UIInputBuilder uiInputBuilder = entityContext.ui().inputBuilder();
        this.assembleActions(uiInputBuilder);

        UIActionHandler actionHandler = uiInputBuilder.findActionHandler(actionID);
        if (actionHandler != null) {
            if (!actionHandler.isEnabled(entityContext)) {
                throw new IllegalArgumentException("Unable to invoke disabled action");
            }
            return actionHandler.handleAction(entityContext, params);
        }
        throw new IllegalArgumentException("Unable to find execution handler for action: <" + actionID + ">. Entity: " + this);
    }
}
