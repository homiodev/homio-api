package org.touchhome.bundle.api.ui.action;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.ActionResponseModel;

import java.util.function.Function;

public interface UISidebarButtonHandler extends Function<EntityContext, ActionResponseModel> {
}
