package org.touchhome.bundle.api.ui.builder;

import org.touchhome.bundle.api.util.NotificationLevel;

public interface BellNotificationBuilder {
    default void danger(String entityID, String title, String value) {
        notification(NotificationLevel.error, entityID, title, value);
    }

    default void info(String entityID, String title, String value) {
        notification(NotificationLevel.info, entityID, title, value);
    }

    default void warn(String entityID, String title, String value) {
        notification(NotificationLevel.warning, entityID, title, value);
    }

    default void success(String entityID, String title, String value) {
        notification(NotificationLevel.success, entityID, title, value);
    }

    void notification(
            NotificationLevel notificationLevel, String entityID, String title, String value);
}
