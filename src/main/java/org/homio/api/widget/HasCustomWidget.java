package org.homio.api.widget;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.homio.api.Context;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.HasJsonData;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.model.Icon;
import org.homio.api.model.JSON;
import org.homio.api.ui.field.action.HasDynamicContextMenuActions;
import org.homio.api.ui.field.action.HasDynamicUIFields;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Implement by entities that able to create CustomWidgetEntity */
public interface HasCustomWidget extends HasDynamicContextMenuActions, HasEntityIdentifier {

  default @Nullable Map<String, CallServiceMethod> getCallServices() {
    return null;
  }

  /** Configure widget fields. Fields will be shown on UI when edit widget */
  void assembleUIFields(
      @NotNull HasDynamicUIFields.UIFieldBuilder uiFieldBuilder, @NotNull HasJsonData sourceEntity);

  /** Calls one or more times when load widget on UI */
  void setWidgetDataStore(
      @NotNull CustomWidgetDataStore customWidgetDataStore,
      @NotNull String widgetEntityID,
      @NotNull JSON widgetData);

  /** Calls when widget not wishes to receive anymore updates */
  void removeWidgetDataStore(@NotNull String widgetEntityID);

  default @NotNull BaseEntity createWidget(
      @NotNull Context context,
      @NotNull String name,
      @NotNull String tabId,
      int width,
      int height) {
    return context
        .widget()
        .createCustomWidget(
            getEntityID(),
            tabId,
            builder -> builder.code(getCode()).css(getStyle()).parameterEntity(getEntityID()));
  }

  /** List of widgets that able to create by impl. class */
  @Nullable
  Map<String, Icon> getAvailableWidgets();

  default int getWidgetHashCode() {
    return String.join(System.lineSeparator(), getCode()).hashCode()
        + String.join(System.lineSeparator(), getStyle()).hashCode();
  }

  default @NotNull List<String> getCode() {
    return CommonUtils.readFile("code.js");
  }

  default @NotNull List<String> getStyle() {
    return CommonUtils.readFile("style.css");
  }

  interface CallServiceMethod {
    @Nullable
    JsonNode callService(@NotNull Context context, @NotNull JsonNode params);
  }
}
