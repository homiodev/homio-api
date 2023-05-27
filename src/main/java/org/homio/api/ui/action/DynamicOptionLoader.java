package org.homio.api.ui.action;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.homio.api.EntityContext;
import org.homio.api.entity.BaseEntity;
import org.homio.api.model.OptionModel;

/**
 * Uses for load option.
 */
public interface DynamicOptionLoader {

    List<OptionModel> loadOptions(DynamicOptionLoaderParameters parameters);

    @Getter
    class DynamicOptionLoaderParameters {
        private final BaseEntity baseEntity;
        private final EntityContext entityContext;
        private final String[] staticParameters;
        private final Map<String, String> dependencies;

        public DynamicOptionLoaderParameters(BaseEntity baseEntity, EntityContext entityContext, String[] staticParameters,
                                             Map<String, String> dependencies) {
            this.baseEntity = baseEntity;
            this.entityContext = entityContext;
            this.staticParameters = staticParameters;
            this.dependencies = dependencies == null ? Collections.emptyMap() : dependencies;
        }
    }
}
