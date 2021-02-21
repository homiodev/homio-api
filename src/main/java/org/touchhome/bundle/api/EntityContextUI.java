package org.touchhome.bundle.api;

import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.touchhome.bundle.api.console.ConsolePlugin;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.exception.ServerException;
import org.touchhome.bundle.api.model.ProgressBar;
import org.touchhome.bundle.api.setting.SettingPluginButton;
import org.touchhome.bundle.api.util.FlowMap;
import org.touchhome.bundle.api.util.NotificationLevel;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.util.Collection;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface EntityContextUI {

    EntityContext getEntityContext();

    @SneakyThrows
    default <T> T runWithProgressAndGet(@NotNull String progressKey, boolean cancellable,
                                        @NotNull ThrowingFunction<ProgressBar, T, Exception> process,
                                        @Nullable Consumer<Exception> finallyBlock) {
        ProgressBar progressBar = (progress, message) -> progress(progressKey, progress, message, cancellable);
        Exception exception = null;
        try {
            progressBar.progress(0, progressKey);
            return process.apply(progressBar);
        } catch (Exception ex) {
            exception = ex;
            throw ex;
        } finally {
            progressBar.done();
            if (finallyBlock != null) {
                finallyBlock.accept(exception);
            }
        }
    }

    @SneakyThrows
    default void runWithProgress(@NotNull String progressKey, boolean cancellable, @NotNull ThrowingConsumer<ProgressBar, Exception> process,
                                 @Nullable Consumer<Exception> finallyBlock) {
        runWithProgressAndGet(progressKey, cancellable, progressBar -> {
            process.accept(progressBar);
            return null;
        }, finallyBlock);
    }

    /**
     * Fire open console window to UI
     */
    default <T extends ConsolePlugin<?>> void openConsole(@NotNull T consolePlugin) {
        sendGlobal(GlobalSendType.openConsole, consolePlugin.getEntityID(), null);
    }

    /**
     * Request to reload window to UI
     */
    default void reloadWindow(@NotNull String reason) {
        sendGlobal(GlobalSendType.reload, reason, null);
    }

    /**
     * Send reload item on UI if related page are opened
     */
    default void updateItem(@NotNull BaseEntity<?> baseEntity) {
        sendGlobal(GlobalSendType.addItem, baseEntity.getEntityID(), baseEntity);
    }

    /**
     * Fire update to ui that entity was changed.
     */
    <T extends BaseEntity> void sendEntityUpdated(T entity);

    void progress(@NotNull String key, double progress, @Nullable String message, boolean cancellable);

    /**
     * Send confirmation message to ui with back handler
     *
     * @param headerButtonAttachTo - if set - attach confirm message to header button
     */
    void sendConfirmation(@NotNull String key, @NotNull String title, @NotNull Runnable confirmHandler,
                          @NotNull Collection<String> messages, @Nullable String headerButtonAttachTo);

    /**
     * Add message to 'bell' header select box
     */
    void addBellNotification(@NotNull String entityID, @NotNull String name, @NotNull String value,
                             @NotNull NotificationLevel notificationLevel);

    /**
     * Add message to 'bell' header select box
     */
    default void addBellInfoNotification(@NotNull String entityID, @NotNull String name, @NotNull String description) {
        addBellNotification(entityID, name, description, NotificationLevel.info);
    }

    /**
     * Add message to 'bell' header select box
     */
    default void addBellWarningNotification(@NotNull String entityID, @NotNull String name, @NotNull String description) {
        addBellNotification(entityID, name, description, NotificationLevel.warning);
    }

    /**
     * Add message to 'bell' header select box
     */
    default void addBellErrorNotification(@NotNull String entityID, @NotNull String name, @NotNull String description) {
        addBellNotification(entityID, name, description, NotificationLevel.error);
    }

    /**
     * Remove message from 'bell' header select box
     */
    void removeBellNotification(@NotNull String entityID);

    // raw
    void sendNotification(@NotNull String destination, @NotNull String param);

    // raw
    void sendNotification(@NotNull String destination, @NotNull JSONObject param);

    default void sendGlobal(@NotNull GlobalSendType type, @NotNull String entityID, @Nullable Object value) {
        sendGlobal(type, entityID, value, null, null);
    }

    default void sendGlobal(@NotNull GlobalSendType type, @Nullable String entityID, @Nullable Object value, @Nullable String title) {
        sendGlobal(type, entityID, value, title, null);
    }

    default void sendGlobal(@NotNull GlobalSendType type, @Nullable String entityID, @Nullable Object value, @Nullable String title, @Nullable JSONObject jsonObject) {
        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }
        sendNotification("-global", jsonObject.put("entityID", entityID).put("type", type.name())
                .putOpt("value", value).putOpt("title", title));
    }

    /**
     * Add button to ui header
     */
    void addHeaderButton(@NotNull String entityID, @Nullable String title, @NotNull String icon, @NotNull String color,
                         boolean rotate, @Nullable Class<? extends SettingPluginButton> hideAction);

    /**
     * Add button to ui header
     */
    void addHeaderButton(@NotNull String entityID, @Nullable String title, @NotNull String color,
                         int duration, @Nullable Class<? extends SettingPluginButton> hideAction);

    /**
     * Remove button from ui header.
     * Header button will be removed only if has no attached elements
     */
    default void removeHeaderButton(@NotNull String entityID) {
        removeHeaderButton(entityID, null, false);
    }

    /**
     * Remove header button on ui
     *
     * @param entityID    - id
     * @param icon        - changed icon if btn has attached elements
     * @param forceRemove - force remove even if header button has attached elements
     */
    void removeHeaderButton(@NotNull String entityID, @Nullable String icon, boolean forceRemove);

    /**
     * Show error toastr message to ui
     */
    default void sendErrorMessage(@NotNull String message) {
        sendErrorMessage(null, message, null, null);
    }

    /**
     * Show error toastr message to ui
     */
    default void sendErrorMessage(@NotNull Exception ex) {
        sendErrorMessage(null, null, null, ex);
    }

    /**
     * Show error toastr message to ui
     */
    default void sendErrorMessage(@NotNull String message, @NotNull Exception ex) {
        sendErrorMessage(null, message, null, ex);
    }

    /**
     * Show error toastr message to ui
     */
    default void sendErrorMessage(@NotNull String title, @NotNull String message) {
        sendErrorMessage(title, message, null, null);
    }

    /**
     * Show error toastr message to ui
     */
    default void sendErrorMessage(@NotNull String title, @NotNull String message, @NotNull Exception ex) {
        sendErrorMessage(title, message, null, ex);
    }

    /**
     * Show error toastr message to ui
     */
    default void sendErrorMessage(@NotNull String message, @NotNull FlowMap messageParam, @NotNull Exception ex) {
        sendErrorMessage(null, message, messageParam, ex);
    }

    /**
     * Show error toastr message to ui
     */
    default void sendErrorMessage(@NotNull String message, @NotNull FlowMap messageParam) {
        sendErrorMessage(null, message, messageParam, null);
    }

    /**
     * Show error toastr message to ui
     */
    default void sendErrorMessage(@Nullable String title, @Nullable String message, @Nullable FlowMap messageParam, @Nullable Exception ex) {
        sendMessage(title, message, NotificationLevel.error, messageParam, ex);
    }

    /**
     * Show info toastr message to ui
     */
    default void sendInfoMessage(@NotNull String message) {
        sendInfoMessage(null, message, null);
    }

    /**
     * Show info toastr message to ui
     */
    default void sendInfoMessage(@NotNull String title, @NotNull String message) {
        sendInfoMessage(title, message, null);
    }

    /**
     * Show info toastr message to ui
     */
    default void sendInfoMessage(@NotNull String message, @NotNull FlowMap messageParam) {
        sendInfoMessage(null, message, messageParam);
    }

    /**
     * Show info toastr message to ui
     */
    default void sendInfoMessage(@Nullable String title, @NotNull String message, @Nullable FlowMap messageParam) {
        sendMessage(title, message, NotificationLevel.info, messageParam, null);
    }

    /**
     * Show success(green) toastr message to ui
     */
    default void sendSuccessMessage(@NotNull String message) {
        sendSuccessMessage(null, message, null);
    }

    /**
     * Show success(green) toastr message to ui
     */
    default void sendSuccessMessage(@NotNull String title, @NotNull String message) {
        sendSuccessMessage(title, message, null);
    }

    /**
     * Show success(green) toastr message to ui
     */
    default void sendSuccessMessage(@NotNull String message, @NotNull FlowMap messageParam) {
        sendSuccessMessage(null, message, messageParam);
    }

    /**
     * Show success(green) toastr message to ui
     */
    default void sendSuccessMessage(@Nullable String title, @NotNull String message, @Nullable FlowMap messageParam) {
        sendMessage(title, message, NotificationLevel.success, messageParam, null);
    }

    /**
     * Show warning(yellow) toastr message to ui
     */
    default void sendWarningMessage(@NotNull String message) {
        sendWarningMessage(null, message, null);
    }

    /**
     * Show warning(yellow) toastr message to ui
     */
    default void sendWarningMessage(@NotNull String title, @NotNull String message) {
        sendWarningMessage(title, message, null);
    }

    /**
     * Show warning(yellow) toastr message to ui
     */
    default void sendWarningMessage(@NotNull String message, @NotNull FlowMap messageParam) {
        sendWarningMessage(null, message, messageParam);
    }

    /**
     * Show warning(yellow) toastr message to ui
     */
    default void sendWarningMessage(@Nullable String title, @NotNull String message, @Nullable FlowMap messageParam) {
        sendMessage(title, message, NotificationLevel.warning, messageParam, null);
    }

    default void sendJsonMessage(@NotNull String title, @NotNull Object json) {
        sendJsonMessage(title, json, null);
    }

    default void sendJsonMessage(@Nullable String title, @NotNull Object json, @Nullable FlowMap messageParam) {
        title = title == null ? null : Lang.getServerMessage(title, messageParam);
        sendGlobal(GlobalSendType.json, null, json, title);
    }

    default void sendMessage(@Nullable String title, @Nullable String message, @Nullable NotificationLevel type, @Nullable FlowMap messageParam, @Nullable Exception ex) {
        title = title == null ? null : Lang.getServerMessage(title, messageParam);
        String text;
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
        popup, json, setting, progress, bell, headerButton, openConsole, confirmation, reload, addItem
    }
}
