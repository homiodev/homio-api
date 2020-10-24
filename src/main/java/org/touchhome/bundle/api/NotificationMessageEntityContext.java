package org.touchhome.bundle.api;

import io.swagger.annotations.ApiParam;
import org.touchhome.bundle.api.json.AlwaysOnTopNotificationEntityJSON;
import org.touchhome.bundle.api.json.NotificationEntityJSON;
import org.touchhome.bundle.api.manager.En;
import org.touchhome.bundle.api.setting.BundleSettingPluginButton;
import org.touchhome.bundle.api.util.FlowMap;
import org.touchhome.bundle.api.util.NotificationType;
import org.touchhome.bundle.api.util.TouchHomeUtils;

public interface NotificationMessageEntityContext {
    default void sendErrorMessage(@ApiParam("message") String message) {
        sendErrorMessage(message, null, null);
    }

    default void sendSuccessMessage(@ApiParam("message") String message) {
        sendNotification(NotificationEntityJSON.success("success-" + message.hashCode()).setName(message));
    }

    default void sendErrorMessage(@ApiParam("message") String message, @ApiParam("ex") Exception ex) {
        sendErrorMessage(message, null, ex);
    }

    void sendNotification(@ApiParam("destination") String destination, @ApiParam("param") Object param);

    default void sendNotification(NotificationEntityJSON notificationEntityJSON) {
        if (notificationEntityJSON != null) {
            sendNotification("-notification", notificationEntityJSON);
        }
    }

    default void showAlwaysOnViewNotification(NotificationEntityJSON json, String icon, String color, Class<? extends BundleSettingPluginButton> stopAction) {
        AlwaysOnTopNotificationEntityJSON topJson = new AlwaysOnTopNotificationEntityJSON(json, color, null, icon);
        if (stopAction != null) {
            topJson.setStopAction("st_" + stopAction.getSimpleName());
        }
        this.addHeaderNotification(topJson);
    }

    default void showAlwaysOnViewNotification(NotificationEntityJSON json, int duration, String color, Class<? extends BundleSettingPluginButton> stopAction) {
        AlwaysOnTopNotificationEntityJSON topJson = new AlwaysOnTopNotificationEntityJSON(json, color, duration, null);
        if (stopAction != null) {
            topJson.setStopAction("st_" + stopAction.getSimpleName());
        }
        this.addHeaderNotification(topJson);
    }

    void hideAlwaysOnViewNotification(NotificationEntityJSON notificationEntityJSON);

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

    default void sendErrorMessage(@ApiParam("message") String message, FlowMap messageParam, Exception ex) {
        if (messageParam != null) {
            message = En.get().getServerMessage(message, messageParam);
        }
        sendNotification(NotificationEntityJSON.danger("error-" + message.hashCode())
                .setName(message).setDescription(ex == null ? null : ("Cause: " + TouchHomeUtils.getErrorMessage(ex))));
    }

    default void sendInfoMessage(@ApiParam("message") String message) {
        sendInfoMessage(message, null);
    }

    default void sendInfoMessage(@ApiParam("message") String message, FlowMap messageParam) {
        if (messageParam != null) {
            message = En.get().getServerMessage(message, messageParam);
        }
        sendNotification(NotificationEntityJSON.info("info-" + message.hashCode()).setName(message));
    }

    default void sendWarnMessage(@ApiParam("message") String message) {
        sendWarnMessage(message, null);
    }

    default void sendWarnMessage(@ApiParam("message") String message, FlowMap messageParam) {
        if (messageParam != null) {
            message = En.get().getServerMessage(message, messageParam);
        }
        sendNotification(NotificationEntityJSON.warn("warn-" + message.hashCode()).setName(message));
    }
}
