package org.touchhome.bundle.api;

import org.touchhome.bundle.api.json.Option;
import org.touchhome.bundle.api.model.BaseEntity;

import java.util.List;

public interface DynamicOptionLoader<T> {

    List<Option> loadOptions(T parameter, BaseEntity baseEntity, EntityContext entityContext);
}
