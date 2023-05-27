package org.homio.api.entity.widget.ability;

import java.util.function.Consumer;
import org.homio.api.EntityContext;
import org.json.JSONObject;

public interface HasUpdateValueListener {
    void addUpdateValueListener(EntityContext entityContext, String key,
                                JSONObject dynamicParameters, Consumer<Object> listener);
}
