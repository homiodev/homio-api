package org.touchhome.bundle.api.optionProvider;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.action.DynamicOptionLoader;

import java.util.Collection;

public class SelectSerialPortOptionLoader implements DynamicOptionLoader {

    @Override
    public Collection<OptionModel> loadOptions(BaseEntity baseEntity, EntityContext entityContext) {
        return OptionModel.listOfPorts(false);
    }
}
