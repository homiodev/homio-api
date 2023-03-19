package org.touchhome.bundle.api.entity.widget.ability;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.storage.SourceHistory;
import org.touchhome.bundle.api.storage.SourceHistoryItem;

import java.util.List;

/**
 * For widget dataSource to fetch simple entity status
 */
public interface HasGetStatusValue extends HasEntityIdentifier, HasUpdateValueListener {
    Object getStatusValue(GetStatusValueRequest request);

    SourceHistory getSourceHistory(GetStatusValueRequest request);

    List<SourceHistoryItem> getSourceHistoryItems(GetStatusValueRequest request, int from, int count);

    /**
     * Uses for UI to determine class type description
     */
    @JsonIgnore
    @SelectDataSourceDescription
    String getGetStatusDescription();

    /**
     * Get current value with unit or whatever to show on ui in popup. May contains HTML
     */
    default String getStatusValueRepresentation(EntityContext entityContext) {
        return null;
    }

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
