package org.touchhome.bundle.api.video;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.state.State;

public interface VideoActionsContext<T extends BaseVideoStreamEntity> {
    State getAttribute(String key);

    T getEntity();

    EntityContext getEntityContext();
}
