package org.homio.bundle.api.widget;

public interface WidgetBaseTemplate {

    String getIcon();

    default String toJavaScript() {
        return null;
    }
}
