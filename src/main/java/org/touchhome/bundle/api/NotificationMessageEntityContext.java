package org.touchhome.bundle.api;

import io.swagger.annotations.ApiParam;
import lombok.Getter;
import org.touchhome.bundle.api.json.NotificationEntityJSON;
import org.touchhome.bundle.api.manager.En;
import org.touchhome.bundle.api.setting.BundleSettingPluginButton;
import org.touchhome.bundle.api.util.NotificationType;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.util.HashMap;
import java.util.Map;

public interface NotificationMessageEntityContext {
    default void sendErrorMessage(@ApiParam("message") String message, @ApiParam("ex") Exception ex) {
        sendErrorMessage(message, null, ex);
    }

    void sendNotification(@ApiParam("destination") String destination, @ApiParam("param") Object param);

    default void sendNotification(@ApiParam("NotificationEntityJSON") NotificationEntityJSON notificationEntityJSON) {
        if (notificationEntityJSON != null) {
            sendNotification("-notification", notificationEntityJSON);
        }
    }

    void showAlwaysOnViewNotification(@ApiParam("NotificationEntityJSON") NotificationEntityJSON notificationEntityJSON, @ApiParam("duration") int duration, @ApiParam("color") String color);

    void showAlwaysOnViewNotification(@ApiParam("NotificationEntityJSON") NotificationEntityJSON notificationEntityJSON,
                                      @ApiParam("icon") String icon, @ApiParam("color") String color,
                                      @ApiParam("stopAction") Class<? extends BundleSettingPluginButton> stopAction);

    void hideAlwaysOnViewNotification(@ApiParam("NotificationEntityJSON") NotificationEntityJSON notificationEntityJSON);

    default void sendNotification(@ApiParam("name") String name, @ApiParam("description") String description, @ApiParam("notificationType") NotificationType notificationType) {
        sendNotification(new NotificationEntityJSON("random-" + System.currentTimeMillis())
                .setName(name)
                .setDescription(description)
                .setNotificationType(notificationType));
    }

    void addHeaderNotification(NotificationEntityJSON notificationEntityJSON);

    default void addHeaderInfoNotification(String key, String name, String description) {
        addHeaderNotification(NotificationEntityJSON.info(key).setName(name).setDescription(description));
    }

    default void addHeaderWarnNotification(String key, String name, String description) {
        addHeaderNotification(NotificationEntityJSON.warn(key).setName(name).setDescription(description));
    }

    default void addHeaderDangerNotification(String key, String name, String description) {
        addHeaderNotification(NotificationEntityJSON.danger(key).setName(name).setDescription(description));
    }

    void removeHeaderNotification(NotificationEntityJSON notificationEntityJSON);

    default void sendErrorMessage(@ApiParam("message") String message, MessageParam messageParam, Exception ex) {
        message = En.get().getServerMessage(message, messageParam);
        sendNotification(NotificationEntityJSON.danger("error-" + message.hashCode())
                .setName(message + ". Cause: " + TouchHomeUtils.getErrorMessage(ex)));
    }

    default void sendInfoMessage(@ApiParam("message") String message) {
        sendInfoMessage(message, null);
    }

    default void sendInfoMessage(@ApiParam("message") String message, MessageParam messageParam) {
        if (messageParam != null) {
            message = En.get().getServerMessage(message, messageParam);
        }
        sendNotification(NotificationEntityJSON.info("info-" + message.hashCode()).setName(message));
    }

    class MessageParam {
        @Getter
        Map<String, String> params = new HashMap<>();

        private MessageParam(String name, String value, String name1, String value1, String name2, String value2) {
            this.params.put(name, value);
            if (name1 != null) {
                this.params.put(name1, value1);
            }
            if (name2 != null) {
                this.params.put(name2, value2);
            }
        }

        public static MessageParam of(String name, String value) {
            return new MessageParam(name, value, null, null, null, null);
        }

        public static MessageParam of(String name, String value, String name1, String value1) {
            return new MessageParam(name, value, name1, value1, null, null);
        }

        public static MessageParam of(String name, String value, String name1, String value1, String name2, String value2) {
            return new MessageParam(name, value, name1, value1, name2, value2);
        }
    }
}
