package org.homio.api.widget;

/** Interface uses to join widget code that holds/update any data with widget on UI */
public interface CustomWidgetDataStore {
  // by calling this method, core will send updates to the UI but not often that 1/sec
  void update(Object data);
}
