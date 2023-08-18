package org.homio.api.entity.widget.ability;

import org.homio.api.EntityContext;
import org.json.JSONObject;

import java.util.function.Consumer;

public interface HasUpdateValueListener {
    void addUpdateValueListener(EntityContext entityContext, String key,
                                JSONObject dynamicParameters, Consumer<Object> listener);
}
