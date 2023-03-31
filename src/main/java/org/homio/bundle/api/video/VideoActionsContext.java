package org.homio.bundle.api.video;

import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.state.State;

public interface VideoActionsContext<T extends BaseVideoStreamEntity> {
    State getAttribute(String key);

    T getEntity();

    EntityContext getEntityContext();
}
