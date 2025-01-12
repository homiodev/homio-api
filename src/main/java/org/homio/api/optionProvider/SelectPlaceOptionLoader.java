package org.homio.api.optionProvider;

import org.homio.api.model.OptionModel;
import org.homio.api.ui.field.selection.dynamic.DynamicOptionLoader;

import java.util.List;

public class SelectPlaceOptionLoader implements DynamicOptionLoader {

  @Override
  public List<OptionModel> loadOptions(DynamicOptionLoaderParameters parameters) {
    return OptionModel.listWithEmpty(parameters.context().setting().getPlaces());
  }
}
