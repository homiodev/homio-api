package org.touchhome.bundle.api;

import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.springframework.lang.Nullable;
import org.touchhome.bundle.api.console.ConsolePlugin;
import org.touchhome.bundle.api.exception.ServerException;
import org.touchhome.bundle.api.setting.SettingPluginButton;
import org.touchhome.bundle.api.util.FlowMap;
import org.touchhome.bundle.api.util.NotificationLevel;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.util.Collection;

public interface EntityContextUI {

    @SneakyThrows
    default <T> T runWithProgressAndGet(String progressKey, ThrowingFunction<String, T, Exception> process,
                                        @Nullable Runnable finallyBlock) {
        try {
            progress(progressKey, 0, progressKey);
            return process.apply(progressKey);
        } finally {
            progressDone(progressKey);
            if (finallyBlock != null) {
                finallyBlock.run();
            }
        }
    }

    @SneakyThrows
    default <T> void runWithProgress(String progressKey, ThrowingConsumer<String, Exception> process,
                                     @Nullable Runnable finallyBlock) {
        runWithProgressAndGet(progressKey, s -> {
            process.accept(s);
            return null;
        }, finallyBlock);
    }

    default <T extends ConsolePlugin<?>> void openConsole(T consolePlugin) {
        sendGlobal(GlobalSendType.openConsole, consolePlugin.getEntityID(), null);
    }

    default <T extends ConsolePlugin<?>> void reloadWindow(String reason) {
        sendGlobal(GlobalSendType.reload, reason, null);
    }

    default void progress(String key, double progress, String status) {
        sendGlobal(GlobalSendType.progress, key, progress, status);
    }

    default void progressDone(String key) {
        sendGlobal(GlobalSendType.progress, key, 100);
    }

    void sendConfirmation(String key, String title, Runnable confirmHandler, String... messages);

    default void sendConfirmation(String key, String title, Runnable confirmHandler, Collection<String> messages) {
        sendConfirmation(key, title, confirmHandler, messages.toArray(new String[0]));
    }

    void addHeaderNotification(String entityID, String name, String value, NotificationLevel notificationLevel);

    default void addHeaderInfoNotification(String entityID, String name, String description) {
        addHeaderNotification(entityID, name, description, NotificationLevel.info);
    }

    default void addHeaderWarningNotification(String entityID, String name, String description) {
        addHeaderNotification(entityID, name, description, NotificationLevel.warning);
    }

    default void addHeaderErrorNotification(String entityID, String name, String description) {
        addHeaderNotification(entityID, name, description, NotificationLevel.error);
    }

    void removeHeaderNotification(String entityID);

    void sendNotification(String destination, Object param);

    default void sendGlobal(GlobalSendType type, String entityID, Object value) {
        sendGlobal(type, entityID, value, null, null);
    }

    default void sendGlobal(GlobalSendType type, String entityID, Object value, String title) {
        sendGlobal(type, entityID, value, title, null);
    }

    default void sendGlobal(GlobalSendType type, String entityID, Object value, String title, JSONObject jsonObject) {
        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }
        sendNotification("-global", jsonObject.put("entityID", entityID).put("type", type.name())
                .put("value", value).putOpt("title", title));
    }

    void showAlwaysOnViewNotification(String entityID, String title, String icon, String color, Integer duration, Class<? extends SettingPluginButton> stopAction);

    void hideAlwaysOnViewNotification(String entityID);

    default void sendErrorMessage(String message) {
        sendErrorMessage(null, message, null, null);
    }

    default void sendErrorMessage(Exception ex) {
        sendErrorMessage(null, null, null, ex);
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
        sendMessage(title, message, NotificationLevel.error, messageParam, ex);
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
        sendMessage(title, message, NotificationLevel.info, messageParam, null);
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
        sendMessage(title, message, NotificationLevel.success, messageParam, null);
    }

    default void sendJsonMessage(String title, Object json) {
        sendJsonMessage(title, json, null);
    }

    default void sendJsonMessage(String title, Object json, FlowMap messageParam) {
        title = title == null ? null : Lang.getServerMessage(title, messageParam);
        sendGlobal(GlobalSendType.json, null, json, title);
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
        sendMessage(title, message, NotificationLevel.warning, messageParam, null);
    }

    default void sendMessage(String title, String message, NotificationLevel type, FlowMap messageParam, Exception ex) {
        title = title == null ? null : Lang.getServerMessage(title, messageParam);
        String text = "";
        if (ex instanceof ServerException) {
            text = Lang.getServerMessage(ex.getMessage(), ((ServerException) ex).getMessageParam() == null ? messageParam : ((ServerException) ex).getMessageParam());
        } else {
            text = message == null ? ex == null ? "Unknown error" : ex.getMessage() : message;
            if (text == null) {
                text = TouchHomeUtils.getErrorMessage(ex);
            }
            // try cast text to lang
            text = Lang.getServerMessage(text, messageParam);
        }
        sendGlobal(GlobalSendType.popup, null, text, title, new JSONObject().put("level", type));
    }

    enum GlobalSendType {
        popup, json, setting, progress, headerNotification, headerButton, openConsole, confirmation, reload
    }
}
