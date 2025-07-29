package org.homio.api.ui.field.selection.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

/**
 * Entity interface that responsible for fetching additional dynamic entity parameters when user
 * select it from select-box
 */
public interface SelectionWithDynamicParameterFields {

  /**
   * Return POJO class with @UIField(...) fields that shows on UI when user select entity
   *
   * @param request - request
   * @return POJO
   */
  DynamicParameterFields getDynamicParameterFields(RequestDynamicParameter request);

  @Getter
  @AllArgsConstructor
  class RequestDynamicParameter {

    private @Nullable Object selectionHolder;
    private @NotNull JSONObject
        metadata; // contains conditions if i.e. for some entities it requires one parameter but for
    // other...
  }
}
