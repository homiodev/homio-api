package org.homio.api.widget;

import org.homio.api.Context;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.HasJsonData;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.model.Icon;
import org.homio.api.model.JSON;
import org.homio.api.ui.field.action.HasDynamicContextMenuActions;
import org.homio.api.ui.field.action.HasDynamicUIFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Implement by entities that able to create CustomWidgetEntity
 */
public interface HasCustomWidget extends
  HasDynamicContextMenuActions, HasEntityIdentifier {

  /**
   * Configure widget fields. Fields will be shown on UI when edit widget
   */
  void assembleUIFields(@NotNull HasDynamicUIFields.UIFieldBuilder uiFieldBuilder,
                        @NotNull HasJsonData sourceEntity);

  /**
   * Calls one or more times when load widget on UI
   */
  void setWidgetDataStore(@NotNull CustomWidgetDataStore customWidgetDataStore,
                          @NotNull String widgetEntityID,
                          @NotNull JSON widgetData);

  /**
   * Calls when widget not wishes to receive anymore updates
   */
  void removeWidgetDataStore(@NotNull String widgetEntityID);

  @NotNull BaseEntity createWidget(@NotNull Context context, @NotNull String name, @NotNull String tabId, int width, int height);

  /**
   * List of widgets that able to create by impl. class
   */
  @Nullable Map<String, Icon> getAvailableWidgets();
}