package org.touchhome.bundle.api.optionProvider;

import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.action.DynamicOptionLoader;

import java.util.Collection;

public class SelectSerialPortOptionLoader implements DynamicOptionLoader {

    @Override
    public Collection<OptionModel> loadOptions(DynamicOptionLoaderParameters parameters) {
        return OptionModel.listOfPorts(false);
    }
}
