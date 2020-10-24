package org.touchhome.bundle.api.ui.action;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.Option;
import org.touchhome.bundle.api.model.BaseEntity;

import java.util.List;

/**
 * Uses for load option.
 */
public interface DynamicOptionLoader<T> {

    List<Option> loadOptions(T parameter, BaseEntity baseEntity, EntityContext entityContext);
}
