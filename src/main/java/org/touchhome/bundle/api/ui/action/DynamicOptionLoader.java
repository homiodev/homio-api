package org.touchhome.bundle.api.ui.action;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.model.OptionModel;

import java.util.Collection;

/**
 * Uses for load option.
 */
public interface DynamicOptionLoader {

    Collection<OptionModel> loadOptions(BaseEntity baseEntity, EntityContext entityContext, String[] staticParameters);
}
