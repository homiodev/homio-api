package org.homio.api.entity.widget.ability;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.homio.api.EntityContext;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.storage.SourceHistory;
import org.homio.api.storage.SourceHistoryItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

/**
 * For widget dataSource to fetch simple entity status
 */
public interface HasGetStatusValue extends HasEntityIdentifier, HasUpdateValueListener {

    Object getStatusValue(@NotNull GetStatusValueRequest request);

    SourceHistory getSourceHistory(@NotNull GetStatusValueRequest request);

    List<SourceHistoryItem> getSourceHistoryItems(@NotNull GetStatusValueRequest request, int from, int count);

    /**
     * @param entityContext -
     * @return Get current value with unit or whatever to show on ui in popup. May contains HTML
     */
    default String getStatusValueRepresentation(@NotNull EntityContext entityContext) {
        return null;
    }

    enum ValueType {
        String, Float, Boolean
    }

    @Getter
    @AllArgsConstructor
    class GetStatusValueRequest {

        private @NotNull EntityContext entityContext;
        private @Nullable JSONObject dynamicParameters;
    }
}
