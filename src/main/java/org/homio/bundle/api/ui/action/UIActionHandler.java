package org.homio.bundle.api.ui.action;

import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.model.ActionResponseModel;
import org.json.JSONObject;

/**
 * Uses for calls on some ui actions i.e. header actions and send result to ui back
 */
public interface UIActionHandler {

    ActionResponseModel handleAction(EntityContext entityContext, JSONObject params) throws Exception;

    default boolean isEnabled(EntityContext entityContext) {
        return true;
    }
}
