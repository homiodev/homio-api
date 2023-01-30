package org.touchhome.bundle.api.optionProvider;

import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.action.DynamicOptionLoader;

import java.util.List;

public class SelectSerialPortOptionLoader implements DynamicOptionLoader {

    @Override
    public List<OptionModel> loadOptions(DynamicOptionLoaderParameters parameters) {
        return OptionModel.listOfPorts(false);
    }
}
