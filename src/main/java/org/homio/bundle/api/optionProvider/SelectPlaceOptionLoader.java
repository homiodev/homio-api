package org.homio.bundle.api.optionProvider;

import java.util.List;
import org.homio.bundle.api.model.OptionModel;
import org.homio.bundle.api.ui.action.DynamicOptionLoader;

public class SelectPlaceOptionLoader implements DynamicOptionLoader {

    @Override
    public List<OptionModel> loadOptions(DynamicOptionLoaderParameters parameters) {
        return OptionModel.listWithEmpty(parameters.getEntityContext().setting().getPlaces());
    }
}
