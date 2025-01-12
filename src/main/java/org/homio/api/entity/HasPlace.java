package org.homio.api.entity;

import jakarta.persistence.Column;
import org.homio.api.optionProvider.SelectPlaceOptionLoader;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.UIFieldType;
import org.homio.api.ui.field.condition.UIFieldShowOnCondition;
import org.homio.api.ui.field.selection.UIFieldSelectConfig;
import org.homio.api.ui.field.selection.dynamic.UIFieldDynamicSelection;

public interface HasPlace extends HasJsonData {

  @Column(length = 64)
  @UIField(order = 30, type = UIFieldType.SelectBox, color = "#538744")
  @UIFieldGroup("GENERAL")
  @UIFieldSelectConfig(selectOnEmptyLabel = "PLACEHOLDER.SELECT_PLACE")
  @UIFieldDynamicSelection(SelectPlaceOptionLoader.class)
  @UIFieldShowOnCondition("return !context.get('compactMode')")
  default String getPlace() {
    return getJsonData("order", "");
  }

  default void setPlace(String value) {
    setJsonData("place", value);
  }
}
