package org.homio.api.ui.field.selection.dynamic;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.homio.api.Context;
import org.homio.api.entity.BaseEntity;
import org.homio.api.model.OptionModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Uses for load option.
 */
public interface DynamicOptionLoader {

  List<OptionModel> loadOptions(DynamicOptionLoaderParameters parameters);

  @Getter
  class DynamicOptionLoaderParameters {

    private final BaseEntity baseEntity;
    private final @Accessors(fluent = true) Context context;
    private final String[] staticParameters;
    private final Map<String, String> dependencies;

    public DynamicOptionLoaderParameters(BaseEntity baseEntity, Context context, String[] staticParameters,
                                         Map<String, String> dependencies) {
      this.baseEntity = baseEntity;
      this.context = context;
      this.staticParameters = staticParameters;
      this.dependencies = dependencies == null ? Collections.emptyMap() : dependencies;
    }
  }
}
