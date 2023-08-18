package org.homio.api.optionProvider;

import org.homio.api.model.OptionModel;
import org.homio.api.ui.action.DynamicOptionLoader;

import java.util.List;

public class SelectSerialPortOptionLoader implements DynamicOptionLoader {

    @Override
    public List<OptionModel> loadOptions(DynamicOptionLoaderParameters parameters) {
        return OptionModel.listOfPorts(false);
    }
}
