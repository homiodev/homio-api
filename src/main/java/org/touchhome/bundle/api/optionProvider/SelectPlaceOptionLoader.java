package org.touchhome.bundle.api.optionProvider;

import org.touchhome.bundle.api.ui.action.DynamicOptionLoader;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.PlaceEntity;

import java.util.List;

public class SelectPlaceOptionLoader implements DynamicOptionLoader {

    @Override
    public List<OptionModel> loadOptions(Object parameter, BaseEntity baseEntity, EntityContext entityContext) {
        return OptionModel.list(entityContext.findAll(PlaceEntity.class));
    }
}
