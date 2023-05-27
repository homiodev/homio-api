package org.homio.api.video;

import org.homio.api.EntityContext;
import org.homio.api.state.State;

public interface VideoActionsContext<T extends BaseVideoStreamEntity> {
    State getAttribute(String key);

    T getEntity();

    EntityContext getEntityContext();
}
