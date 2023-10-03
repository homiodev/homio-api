package org.homio.api.entity.widget.ability;

import java.util.function.Consumer;
import org.homio.api.EntityContext;
import org.homio.api.state.State;
import org.json.JSONObject;

public interface HasUpdateValueListener {

    void addUpdateValueListener(EntityContext entityContext, String discriminator,
        JSONObject dynamicParameters, Consumer<State> listener);
}
