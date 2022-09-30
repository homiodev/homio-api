package org.touchhome.bundle.api.optionProvider;

import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.action.DynamicOptionLoader;

import java.util.List;

public class SelectPlaceOptionLoader implements DynamicOptionLoader {

    @Override
    public List<OptionModel> loadOptions(DynamicOptionLoaderParameters parameters) {
        return OptionModel.listWithEmpty(parameters.getEntityContext().setting().getPlaces());
    }
}
