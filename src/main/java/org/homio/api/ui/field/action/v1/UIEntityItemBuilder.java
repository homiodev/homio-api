package org.homio.api.ui.field.action.v1;

import org.homio.api.model.Icon;
import org.jetbrains.annotations.NotNull;

public interface UIEntityItemBuilder<Owner, Value> extends UIEntityBuilder {

  String getSeparatedText();

  Owner setSeparatedText(String text);

  String getStyle();

  Owner setValue(Value value);

  Owner setTitle(String title);

  Owner setDisabled(boolean disabled);

  Owner setOrder(int order);

  Owner setIcon(Icon icon);

  Owner setColor(String color);

  Owner appendStyle(@NotNull String style, @NotNull String value);

  Owner setOuterClass(String outerClass);

  Owner addFetchValueHandler(String key, Runnable fetchValueHandler);
}
