package org.homio.api.entity.widget.ability;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.homio.api.Context;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.storage.SourceHistory;
import org.homio.api.storage.SourceHistoryItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

/** For widget dataSource to fetch simple entity status */
public interface HasGetStatusValue extends HasEntityIdentifier, HasUpdateValueListener {

  Object getStatusValue(@NotNull GetStatusValueRequest request);

  ValueType getValueType();

  SourceHistory getSourceHistory(@NotNull GetStatusValueRequest request);

  List<SourceHistoryItem> getSourceHistoryItems(
      @NotNull GetStatusValueRequest request, int from, int count);

  /**
   * @param context -
   * @return Get current value with unit or whatever to show on ui in popup. May contains HTML
   */
  default String getStatusValueRepresentation(@NotNull Context context) {
    return null;
  }

  enum ValueType {
    String,
    Float,
    Boolean,
    Unknown
  }

  @Getter
  @AllArgsConstructor
  class GetStatusValueRequest {

    private @NotNull @Accessors(fluent = true) Context context;
    private @Nullable JSONObject dynamicParameters;
  }
}
