package org.touchhome.bundle.api.ui.action;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.ActionResponseModel;

import java.util.function.BiFunction;

public interface UIActionHandler extends BiFunction<EntityContext, JSONObject, ActionResponseModel> {
}
