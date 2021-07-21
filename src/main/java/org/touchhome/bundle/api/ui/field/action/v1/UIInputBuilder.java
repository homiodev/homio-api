package org.touchhome.bundle.api.ui.field.action.v1;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;

import java.util.List;
import java.util.function.Consumer;

public interface UIInputBuilder {
    EntityContext getEntityContext();

    List<UIEntityBuilder> getUiEntities();

    UISelectableButtonEntityBuilder addSelectableButton(String name, String icon, Consumer<JSONObject> action);
}
