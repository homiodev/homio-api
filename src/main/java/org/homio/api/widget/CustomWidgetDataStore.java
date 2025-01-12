package org.homio.api.widget;

/**
 * Interface uses to join widget code that hold/update any data with widget on UI
 */
public interface CustomWidgetDataStore {
  // by calling this method core will send updates to UI but not oftern that 1/sec
  void update(Object data);
}
