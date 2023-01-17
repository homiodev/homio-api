package org.touchhome.bundle.api.entity.widget.ability;

import java.util.function.Consumer;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;

public interface HasUpdateValueListener {
    void addUpdateValueListener(
            EntityContext entityContext,
            String key,
            JSONObject dynamicParameters,
            Consumer<Object> listener);
}
