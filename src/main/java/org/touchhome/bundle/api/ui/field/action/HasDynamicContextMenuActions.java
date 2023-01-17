package org.touchhome.bundle.api.ui.field.action;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.ui.action.UIActionHandler;
import org.touchhome.bundle.api.ui.field.action.v1.UIInputBuilder;

/** For BaseItems that wants dynamic context menu items */
public interface HasDynamicContextMenuActions {
    void assembleActions(UIInputBuilder uiInputBuilder);

    default ActionResponseModel handleAction(
            EntityContext entityContext, String actionID, JSONObject params) throws Exception {
        UIInputBuilder uiInputBuilder = entityContext.ui().inputBuilder();
        this.assembleActions(uiInputBuilder);

        UIActionHandler actionHandler = uiInputBuilder.findActionHandler(actionID);
        if (actionHandler != null) {
            if (!actionHandler.isEnabled(entityContext)) {
                throw new IllegalArgumentException("Unable to invoke disabled action");
            }
            return actionHandler.handleAction(entityContext, params);
        }
        throw new IllegalArgumentException(
                "Unable to find execution handler for action: <" + actionID + ">. Entity: " + this);
    }
}
