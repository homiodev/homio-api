package org.touchhome.bundle.api.ui.action;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.ActionResponseModel;

/**
 * Uses for calls on some ui actions i.e. header actions and send result to ui back
 */
public interface UIActionHandler {

    ActionResponseModel handleAction(EntityContext entityContext, JSONObject params) throws Exception;

    default boolean isEnabled(EntityContext entityContext) {
        return true;
    }
}
