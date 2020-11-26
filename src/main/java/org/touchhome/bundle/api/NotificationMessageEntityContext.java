package org.touchhome.bundle.api;

import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.json.AlwaysOnTopNotificationEntityJSON;
import org.touchhome.bundle.api.json.NotificationEntityJSON;
import org.touchhome.bundle.api.manager.En;
import org.touchhome.bundle.api.setting.BundleSettingPluginButton;
import org.touchhome.bundle.api.util.FlowMap;
import org.touchhome.bundle.api.util.NotificationType;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.util.Collection;

public interface NotificationMessageEntityContext {

    void sendConfirmation(String key, String title, Runnable confirmHandler, String... messages);

    default void sendConfirmation(String key, String title, Runnable confirmHandler, Collection<String> messages) {
        sendConfirmation(key, title, confirmHandler, messages.toArray(new String[0]));
    }

    void addHeaderNotification(NotificationEntityJSON notificationEntityJSON);

    default void addHeaderInfoNotification(String key, String name, String description) {
        addHeaderNotification(NotificationEntityJSON.info(key).setName(name).setDescription(description));
    }

    default void addHeaderWarningNotification(String key, String name, String description) {
        addHeaderNotification(NotificationEntityJSON.warn(key).setName(name).setDescription(description));
    }

    default void addHeaderErrorNotification(String key, String name, String description) {
        addHeaderNotification(NotificationEntityJSON.danger(key).setName(name).setDescription(description));
    }

    void removeHeaderNotification(NotificationEntityJSON notificationEntityJSON);

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

    default void showAlwaysOnViewNotification(String name, String icon, String color) {
        showAlwaysOnViewNotification(new NotificationEntityJSON(name), icon, color, null);
    }

    default void showAlwaysOnViewNotification(NotificationEntityJSON json, int duration, String color, Class<? extends BundleSettingPluginButton> stopAction) {
        AlwaysOnTopNotificationEntityJSON topJson = new AlwaysOnTopNotificationEntityJSON(json, color, duration, null);
        if (stopAction != null) {
            topJson.setStopAction("st_" + stopAction.getSimpleName());
        }
        this.addHeaderNotification(topJson);
    }

    void hideAlwaysOnViewNotification(String key);

    default void sendErrorMessage(String message) {
        sendErrorMessage(null, message, null, null);
    }

    default void sendErrorMessage(String message, Exception ex) {
        sendErrorMessage(null, message, null, ex);
    }

    default void sendErrorMessage(String title, String message) {
        sendErrorMessage(title, message, null, null);
    }

    default void sendErrorMessage(String title, String message, Exception ex) {
        sendErrorMessage(title, message, null, ex);
    }

    default void sendErrorMessage(String message, FlowMap messageParam, Exception ex) {
        sendErrorMessage(null, message, messageParam, ex);
    }

    default void sendErrorMessage(String message, FlowMap messageParam) {
        sendErrorMessage(null, message, messageParam, null);
    }

    default void sendErrorMessage(String title, String message, FlowMap messageParam, Exception ex) {
        sendMessage(title, message, NotificationType.error, messageParam, ex);
    }

    default void sendInfoMessage(String message) {
        sendInfoMessage(null, message, null);
    }

    default void sendInfoMessage(String title, String message) {
        sendInfoMessage(title, message, null);
    }

    default void sendInfoMessage(String message, FlowMap messageParam) {
        sendInfoMessage(null, message, messageParam);
    }

    default void sendInfoMessage(String title, String message, FlowMap messageParam) {
        sendMessage(title, message, NotificationType.info, messageParam, null);
    }

    default void sendSuccessMessage(String message) {
        sendSuccessMessage(null, message, null);
    }

    default void sendSuccessMessage(String title, String message) {
        sendSuccessMessage(title, message, null);
    }

    default void sendSuccessMessage(String message, FlowMap messageParam) {
        sendSuccessMessage(null, message, messageParam);
    }

    default void sendSuccessMessage(String title, String message, FlowMap messageParam) {
        sendMessage(title, message, NotificationType.success, messageParam, null);
    }

    default void sendWarningMessage(String message) {
        sendWarningMessage(null, message, null);
    }

    default void sendWarningMessage(String title, String message) {
        sendWarningMessage(title, message, null);
    }

    default void sendWarningMessage(String message, FlowMap messageParam) {
        sendWarningMessage(null, message, messageParam);
    }

    default void sendWarningMessage(String title, String message, FlowMap messageParam) {
        sendMessage(title, message, NotificationType.warning, messageParam, null);
    }

    default void sendMessage(String title, String message, NotificationType type, FlowMap messageParam, Exception ex) {
        title = title == null ? null : En.getServerMessage(title, messageParam);
        message = message == null ? null : En.getServerMessage(message, messageParam);
        String text = StringUtils.defaultString(message, "") + (ex == null ? "" : "Cause: " + TouchHomeUtils.getErrorMessage(ex));
        sendNotification(new NotificationEntityJSON(String.valueOf(System.currentTimeMillis()))
                .setName(title).setNotificationType(type).setDescription(text));
    }
}
