package org.touchhome.bundle.api.entity.widget.ability;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

/**
 * For widget dataSource to fetch simple entity status
 */
public interface HasGetStatusValue extends HasEntityIdentifier, HasUpdateValueListener {
    Object getStatusValue(GetStatusValueRequest request);

    /**
     * Uses for UI to determine class type description
     */
    @SelectDataSourceDescription
    String getGetStatusDescription();

    enum ValueType {
        String, Float, Boolean
    }

    @Getter
    @AllArgsConstructor
    class GetStatusValueRequest {
        private EntityContext entityContext;
        private JSONObject dynamicParameters;
    }
}
