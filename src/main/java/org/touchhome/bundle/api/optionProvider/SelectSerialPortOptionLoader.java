package org.touchhome.bundle.api.optionProvider;

import java.util.List;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.action.DynamicOptionLoader;

public class SelectSerialPortOptionLoader implements DynamicOptionLoader {

    @Override
    public List<OptionModel> loadOptions(DynamicOptionLoaderParameters parameters) {
        return OptionModel.listOfPorts(false);
    }
}
