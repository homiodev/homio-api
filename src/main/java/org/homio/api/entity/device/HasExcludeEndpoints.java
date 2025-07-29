package org.homio.api.entity.device;

import java.util.List;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.UIFieldNoReadDefaultValue;
import org.homio.api.ui.field.condition.UIFieldShowOnCondition;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an interface for managing exclude endpoints from device Core lib manages for removing
 * variables on startup, etc...
 */
public interface HasExcludeEndpoints extends DeviceEndpointsBehaviourContract {

  @UIField(order = 500)
  @UIFieldGroup("GENERAL")
  @UIFieldShowOnCondition("return !context.get('compactMode')")
  @UIFieldNoReadDefaultValue
  @NotNull
  default List<String> getExcludeEndpoints() {
    return getJsonDataList("excludeEp");
  }

  default void setExcludeEndpoints(String value) {
    setJsonDataAsSet("excludeEp", value);
  }
}
