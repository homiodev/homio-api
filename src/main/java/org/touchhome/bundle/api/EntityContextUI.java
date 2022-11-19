package org.touchhome.bundle.api;

import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.touchhome.bundle.api.console.ConsolePlugin;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.setting.SettingPluginButton;
import org.touchhome.bundle.api.ui.dialog.DialogModel;
import org.touchhome.bundle.api.ui.field.action.ActionInputParameter;
import org.touchhome.bundle.api.ui.field.action.v1.UIInputBuilder;
import org.touchhome.bundle.api.util.NotificationLevel;
import org.touchhome.common.exception.ServerException;
import org.touchhome.common.model.ProgressBar;
import org.touchhome.common.util.CommonUtils;
import org.touchhome.common.util.FlowMap;
import org.touchhome.common.util.Lang;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public interface EntityContextUI {

    EntityContext getEntityContext();

    UIInputBuilder inputBuilder();

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
    default void runWithProgress(@NotNull String progressKey, boolean cancellable,
                                 @NotNull ThrowingConsumer<ProgressBar, Exception> process,
                                 @Nullable Consumer<Exception> finallyBlock) {
        runWithProgressAndGet(progressKey, cancellable, progressBar -> {
            process.accept(progressBar);
            return null;
        }, finallyBlock);
    }

    /**
     * Register console plugin name. In case if console plugin available only if some
     * entity is created or not enabled by some case we may show disabled console name on UI
     */
    void registerConsolePluginName(@NotNull String name);

    <T extends ConsolePlugin> void registerConsolePlugin(@NotNull String name, @NotNull T plugin);

    <T extends ConsolePlugin> T getRegisteredConsolePlugin(@NotNull String name);

    boolean unRegisterConsolePlugin(@NotNull String name);

    /**
     * Fire open console window to UI
     */
    <T extends ConsolePlugin<?>> void openConsole(@NotNull T consolePlugin);

    /**
     * Request to reload window to UI
     */
    void reloadWindow(@NotNull String reason);

    /**
     * Send reload item on UI if related page are opened
     */
    void updateItem(@NotNull BaseEntity<?> baseEntity);

    /**
     * Fire update to ui that entity was changed.
     */
    <T extends BaseEntity> void sendEntityUpdated(T entity);

    void progress(@NotNull String key, double progress, @Nullable String message, boolean cancellable);

    default void sendConfirmation(@NotNull String key, @NotNull String title, @NotNull Runnable confirmHandler,
                                  @NotNull Collection<String> messages, @Nullable String headerButtonAttachTo) {
        sendConfirmation(key, title, responseType -> {
            if (responseType == DialogResponseType.Accepted) {
                confirmHandler.run();
            }
        }, messages, 0, headerButtonAttachTo);
    }

    /**
     * Send confirmation message to ui with back handler
     *
     * @param headerButtonAttachTo - if set - attach confirm message to header button
     */
    default void sendConfirmation(@NotNull String key, @NotNull String title,
                                  @NotNull Consumer<DialogResponseType> confirmHandler,
                                  @NotNull Collection<String> messages, int maxTimeoutInSec,
                                  @Nullable String headerButtonAttachTo) {
        sendDialogRequest(key, title, (responseType, pressedButton, parameters) ->
                confirmHandler.accept(responseType), dialogModel -> {
            List<ActionInputParameter> inputs = messages.stream().map(ActionInputParameter::message).collect(Collectors.toList());
            dialogModel.headerButtonAttachTo(headerButtonAttachTo)
                    .submitButton("Confirm", button -> {
                    }).group("General", inputs);
        });
    }

    /**
     * Send request dialog to ui
     */
    void sendDialogRequest(@NotNull DialogModel dialogModel);

    default void sendDialogRequest(@NotNull String key, @NotNull String title, DialogRequestHandler actionHandler,
                                   Consumer<DialogModel> dialogBuilderSupplier) {
        DialogModel dialogModel = new DialogModel(key, title, actionHandler);
        dialogBuilderSupplier.accept(dialogModel);
        sendDialogRequest(dialogModel);
    }

    /**
     * Add message to 'bell' header select box
     */
    void addBellNotification(@NotNull String entityID, @NotNull String name, @NotNull String value,
                             @NotNull NotificationLevel notificationLevel, @Nullable Consumer<UIInputBuilder> actionBuilder);

    /**
     * Add message to 'bell' header select box
     */
    default void addBellInfoNotification(@NotNull String entityID, @NotNull String name, @NotNull String description) {
        addBellInfoNotification(entityID, name, description, null);
    }

    default void addBellInfoNotification(@NotNull String entityID, @NotNull String name, @NotNull String description,
                                         @Nullable Consumer<UIInputBuilder> actionBuilder) {
        addBellNotification(entityID, name, description, NotificationLevel.info, actionBuilder);
    }

    /**
     * Add message to 'bell' header select box
     */
    default void addBellWarningNotification(@NotNull String entityID, @NotNull String name, @NotNull String description) {
        addBellWarningNotification(entityID, name, description, null);
    }

    default void addBellWarningNotification(@NotNull String entityID, @NotNull String name, @NotNull String description,
                                            @Nullable Consumer<UIInputBuilder> actionBuilder) {
        addBellNotification(entityID, name, description, NotificationLevel.warning, actionBuilder);
    }

    /**
     * Add message to 'bell' header select box
     */
    default void addBellErrorNotification(@NotNull String entityID, @NotNull String name, @NotNull String description) {
        addBellErrorNotification(entityID, name, description, null);
    }

    default void addBellErrorNotification(@NotNull String entityID, @NotNull String name, @NotNull String description,
                                          @Nullable Consumer<UIInputBuilder> actionBuilder) {
        addBellNotification(entityID, name, description, NotificationLevel.error, actionBuilder);
    }

    /**
     * Remove message from 'bell' header select box
     */
    void removeBellNotification(@NotNull String entityID);

    // raw
    void sendNotification(@NotNull String destination, @NotNull String param);

    // raw
    void sendNotification(@NotNull String destination, @NotNull JSONObject param);

    /**
     * Add button to ui header
     */
    void addHeaderButton(@NotNull String entityID, @NotNull String color, @Nullable String title, @Nullable String icon,
                         boolean rotate, @Nullable Integer border, @Nullable Integer duration,
                         @Nullable Class<? extends BaseEntity> page, @Nullable Class<? extends SettingPluginButton> hideAction);

    default void addHeaderButton(@NotNull String entityID, @NotNull String color, @Nullable String title, @Nullable String icon) {
        addHeaderButton(entityID, color, title, icon, false, null, null, null, null);
    }

    default void addHeaderButton(@NotNull String entityID, @NotNull String color, int duration, @Nullable String title) {
        addHeaderButton(entityID, color, title, null, false, null, duration, null, null);
    }

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
    default void sendErrorMessage(@Nullable String title, @Nullable String message, @Nullable FlowMap messageParam,
                                  @Nullable Exception ex) {
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

    void sendJsonMessage(@Nullable String title, @NotNull Object json, @Nullable FlowMap messageParam);

    default void sendMessage(@Nullable String title, @Nullable String message, @Nullable NotificationLevel level,
                             @Nullable FlowMap messageParam, @Nullable Exception ex) {
        title = title == null ? null : Lang.getServerMessage(title, messageParam);
        String text;
        if (ex instanceof ServerException) {
            text = ((ServerException) ex).toString(messageParam);
        } else {
            text = StringUtils.isEmpty(message) ? ex == null ? "Unknown error" : ex.getMessage() : message;
            if (text == null) {
                text = CommonUtils.getErrorMessage(ex);
            }
            // try cast text to lang
            text = Lang.getServerMessage(text, messageParam);
        }
        sendMessage(title, text, level);
    }

    void sendMessage(@Nullable String title, @Nullable String message, @Nullable NotificationLevel level);

    enum DialogResponseType {
        Cancelled, Timeout, Accepted
    }

    interface DialogRequestHandler {
        void handle(DialogResponseType responseType, String pressedButton, JSONObject parameters);
    }
}
