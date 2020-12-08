package org.touchhome.bundle.api.optionProvider;

import org.touchhome.bundle.api.ui.action.DynamicOptionLoader;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.entity.BaseEntity;

import java.util.Collection;
import java.util.List;

public class SelectSerialPortOptionLoader implements DynamicOptionLoader {

    @Override
    public Collection<OptionModel> loadOptions(Object parameter, BaseEntity baseEntity, EntityContext entityContext) {
        return OptionModel.listOfPorts(false);
    }
}
