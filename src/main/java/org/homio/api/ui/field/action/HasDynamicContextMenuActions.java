package org.homio.api.ui.field.action;

import org.homio.api.Context;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.ui.UIActionHandler;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.json.JSONObject;

/**
 * For BaseItems that wants dynamic context menu items
 */
public interface HasDynamicContextMenuActions {

    void assembleActions(UIInputBuilder uiInputBuilder);

    default ActionResponseModel handleAction(Context context, String actionID, JSONObject params) throws Exception {
        UIInputBuilder uiInputBuilder = context.ui().inputBuilder();
        this.assembleActions(uiInputBuilder);

        UIActionHandler actionHandler = uiInputBuilder.findActionHandler(actionID);
        if (actionHandler != null) {
            if (!actionHandler.isEnabled(context)) {
                throw new IllegalArgumentException("Unable to invoke disabled action");
            }
            return actionHandler.handleAction(context, params);
        }
        throw new IllegalArgumentException("Unable to find execution handler for action: <" + actionID + ">. Entity: " + this);
    }
}
