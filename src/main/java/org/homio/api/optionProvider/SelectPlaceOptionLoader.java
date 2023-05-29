package org.homio.api.optionProvider;

import java.util.List;
import org.homio.api.model.OptionModel;
import org.homio.api.ui.action.DynamicOptionLoader;

public class SelectPlaceOptionLoader implements DynamicOptionLoader {

    @Override
    public List<OptionModel> loadOptions(DynamicOptionLoaderParameters parameters) {
        return OptionModel.listWithEmpty(parameters.getEntityContext().setting().getPlaces());
    }
}