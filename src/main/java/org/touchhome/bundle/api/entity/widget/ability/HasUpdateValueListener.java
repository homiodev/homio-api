package org.touchhome.bundle.api.entity.widget.ability;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;

import java.util.function.Consumer;

public interface HasUpdateValueListener {
    void addUpdateValueListener(EntityContext entityContext, String key,
                                JSONObject dynamicParameters, Consumer<Object> listener);
}
