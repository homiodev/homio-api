package org.homio.api.ui;

import org.homio.api.Context;
import org.homio.api.model.ActionResponseModel;
import org.json.JSONObject;

/**
 * Uses for calls on some ui actions i.e. header actions and send result to ui back
 */
public interface UIActionHandler {

    ActionResponseModel handleAction(Context context, JSONObject params) throws Exception;

    default boolean isEnabled(Context context) {
        return true;
    }
}
