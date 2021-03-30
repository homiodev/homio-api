package org.touchhome.bundle.api.ui.action;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.ActionResponseModel;

import java.util.function.BiFunction;

/**
 * Uses for calls on some ui actions i.e. header actions and send result to ui back
 */
public interface UIActionHandler extends BiFunction<EntityContext, JSONObject, ActionResponseModel> {
}
