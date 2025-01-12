package org.homio.api;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pivovarit.function.ThrowingBiFunction;
import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.console.ConsolePlugin;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.BaseEntityIdentifier;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.entity.version.HasFirmwareVersion;
import org.homio.api.exception.ServerException;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.Icon;
import org.homio.api.model.OptionModel;
import org.homio.api.model.Status;
import org.homio.api.setting.SettingPluginButton;
import org.homio.api.stream.ContentStream;
import org.homio.api.ui.UI.Color;
import org.homio.api.ui.UIActionHandler;
import org.homio.api.ui.dialog.DialogModel;
import org.homio.api.ui.field.action.ActionInputParameter;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.homio.api.ui.field.action.v1.layout.UIFlexLayoutBuilder;
import org.homio.api.ui.field.action.v1.layout.UILayoutBuilder;
import org.homio.api.util.FlowMap;
import org.homio.api.util.Lang;
import org.homio.api.util.NotificationLevel;
import org.homio.hquery.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.homio.api.util.CommonUtils.getErrorMessage;

@SuppressWarnings("unused")
public interface ContextUI {

  @NotNull
  ContextUI.ContextUIToastr toastr();

  @NotNull
  Context context();

  @NotNull
  UIInputBuilder inputBuilder();

  @NotNull
  ContextUI.ContextUINotification notification();

  @NotNull
  ContextUI.ContextUIConsole console();

  @NotNull
  ContextUI.ContextUIDialog dialog();

  @NotNull
  ContextUI.ContextUIProgress progress();

  @NotNull
  ContextUI.ContextUIMedia media();

  void registerUIImage(@NotNull String name, @NotNull String base64Image);

  /**
   * Assign context menu action to specific entity on UI
   *
   * @param entityID - item id
   * @param key      - unique key
   * @param builder  - builder
   */
  void addItemContextMenu(@NotNull String entityID, @NotNull String key, @NotNull Consumer<UIInputBuilder> builder);

  void removeItem(@NotNull BaseEntity baseEntity);

  void updateItem(@NotNull BaseEntity baseEntity);

  default void updateItems(@NotNull Class<? extends BaseEntity> baseEntityClass) {
    for (BaseEntity baseEntity : context().db().findAll(baseEntityClass)) {
      updateItem(baseEntity);
    }
  }

  /**
   * Update specific field
   *
   * @param baseEntity  -
   * @param updateField -
   * @param value       -
   */
  void updateItem(@NotNull BaseEntityIdentifier baseEntity, @NotNull String updateField, @Nullable Object value);

  /**
   * Update specific field inside @UIFieldInlineEntities
   *
   * @param parentEntity    - holder entity entity. i.e.: ZigBeeDeviceEntity
   * @param parentFieldName - parent field name that holds Set of destinations. i.e.: 'endpoints'
   * @param innerEntityID   - target field entity ID to update from inside Set
   * @param updateField     - specific field name to update inside innerEntity
   * @param value           - value to send to UI
   */
  void updateInnerSetItem(@NotNull BaseEntityIdentifier parentEntity, @NotNull String parentFieldName, @NotNull String innerEntityID,
                          @NotNull String updateField, @NotNull Object value);

  void sendDynamicUpdate(@NotNull String dynamicUpdateID, @NotNull Object value);

  void sendRawData(@NotNull String destination, @NotNull String param);

  void sendRawData(@NotNull String destination, @NotNull ObjectNode param);

  // Add button to ui header
  HeaderButtonBuilder headerButtonBuilder(@NotNull String key);

  /**
   * Remove button from ui header. Header button will be removed only if it has no attached elements
   *
   * @param key -
   */
  default void removeHeaderButton(@NotNull String key) {
    removeHeaderButton(key, null, false);
  }

  /**
   * Remove header button on ui
   *
   * @param key         - id
   * @param icon        - changed icon if btn has attached elements
   * @param forceRemove - force remove even if header button has attached elements
   */
  void removeHeaderButton(@NotNull String key, @Nullable String icon, boolean forceRemove);

  default void sendJsonMessage(@NotNull String title, @NotNull Object json) {
    sendJsonMessage(title, json, null);
  }

  void sendJsonMessage(@Nullable String title, @NotNull Object json, @Nullable FlowMap messageParam);

  enum DialogResponseType {
    Cancelled, Timeout, Accepted
  }

  interface DialogRequestHandler {

    void handle(@NotNull DialogResponseType responseType, @NotNull String pressedButton, @NotNull ObjectNode parameters);
  }

  interface HeaderButtonBuilder {

    @NotNull
    HeaderButtonBuilder title(@NotNull String title);

    @NotNull
    HeaderButtonBuilder icon(@NotNull Icon icon);

    /**
     * Set border
     *
     * @param width - default 1
     * @param color - default unset
     * @return this
     */
    @NotNull
    HeaderButtonBuilder border(int width, @Nullable String color);

    /**
     * Button available duration
     *
     * @param duration time for duration
     * @return this
     */
    @NotNull
    HeaderButtonBuilder duration(int duration);

    /**
     * Specify HeaderButton only available for specific page
     *
     * @param page - page id
     * @return - this
     */
    @NotNull
    HeaderButtonBuilder availableForPage(@NotNull Class<? extends BaseEntity> page);

    @NotNull
    HeaderButtonBuilder clickAction(@NotNull Class<? extends SettingPluginButton> clickAction);

    @NotNull
    HeaderButtonBuilder clickAction(@NotNull Supplier<ActionResponseModel> clickAction);

    @NotNull
    HeaderButtonBuilder attachToHeaderMenu(@NotNull String name);

    void build();
  }

  interface NotificationBlockBuilder {

    /**
     * Move to entity if click on header block title
     *
     * @param entity - entity to link to
     * @return this
     */
    @NotNull
    NotificationBlockBuilder linkToEntity(@NotNull BaseEntity entity);

    @NotNull
    NotificationBlockBuilder visibleForUser(@NotNull String email);

    @NotNull
    NotificationBlockBuilder blockActionBuilder(@NotNull Consumer<UIInputBuilder> builder);

    @NotNull
    NotificationBlockBuilder addFlexAction(@NotNull String key, @NotNull Consumer<UIFlexLayoutBuilder> builder);

    @NotNull
    NotificationBlockBuilder contextMenuActionBuilder(@NotNull Consumer<UIInputBuilder> builder);

    @NotNull
    NotificationBlockBuilder setNameColor(@Nullable String color);

    default @NotNull NotificationBlockBuilder setDevices(@Nullable Collection<? extends DeviceBaseEntity> devices) {
      if (devices != null) {
        addInfo("sum", new Icon("fas fa-mountain-city", "#CDDC39"), Lang.getServerMessage("TITLE.DEVICES_STAT",
          FlowMap.of("ONLINE", devices.stream().filter(d -> d.getStatus().isOnline()).count(), "TOTAL", devices.size())));
        if (devices.isEmpty()) {
          return this;
        }
        contextMenuActionBuilder(contextAction -> {
          for (DeviceBaseEntity device : devices) {
            String name = device instanceof DeviceEndpointsBehaviourContract
              ? ((DeviceEndpointsBehaviourContract) device).getDeviceFullName() :
              device.getTitle();
            contextAction.addInfo(name)
              .setColor(device.getStatus().getColor())
              .setIcon(device.getEntityIcon())
              .linkToEntity(device);
          }
        });
      }
      return this;
    }

    /**
     * Specify whole notification block status and uses for getting border color
     *
     * @param status - block status
     * @return this
     */
    @NotNull
    NotificationBlockBuilder setStatus(@Nullable Status status);

    /**
     * Run handler on every user fetch url
     */
    @NotNull
    NotificationBlockBuilder fireOnFetch(@NotNull Runnable handler);

    /**
     * Set 'Update' button if firmware already installing or not
     *
     * @param value - true if need disable 'update' button
     * @return this
     */
    @NotNull
    NotificationBlockBuilder setUpdating(boolean value);

    /**
     * Specify custom border color. Default takes color from Status if present. If border and status not specified than fetch all rows from block and check
     * if it has status info. If all rows has ONLINE, ...
     *
     * @param color - color in hex format
     * @return this
     */
    @NotNull
    NotificationBlockBuilder setBorderColor(@Nullable String color);

    /**
     * Set notification block version
     *
     * @param version - version string
     * @return builder
     */
    @NotNull
    NotificationBlockBuilder setVersion(@Nullable String version);

    /**
     * Add updatable button to ui notification block.
     *
     * @param updateHandler - handler to execute when user press update button. Execution executes inside thread with passing progressBar object and
     *                      selected 'version'
     * @param versions      - list of versions to be able to select from UI select box
     * @return builder
     */
    @NotNull
    NotificationBlockBuilder setUpdatable(@NotNull ThrowingBiFunction<ProgressBar, String, ActionResponseModel, Exception> updateHandler,
                                          @NotNull List<OptionModel> versions);

    default @NotNull NotificationBlockBuilder setUpdatable(@NotNull HasFirmwareVersion firmwareEntity) {
      List<OptionModel> versions = firmwareEntity.getNewAvailableVersion();
      if (versions != null) {
        setUpdatable(firmwareEntity::update, versions);
      }
      setVersion(firmwareEntity.getFirmwareVersion());
      setUpdating(firmwareEntity.isFirmwareUpdating());
      return this;
    }

    default @NotNull NotificationInfoLineBuilder addInfo(@NotNull String info, @Nullable Icon icon) {
      return addInfo(String.valueOf(info.hashCode()), icon, info);
    }

    default @NotNull NotificationBlockBuilder addErrorStatusInfo(@Nullable String message) {
      if (StringUtils.isNotEmpty(message)) {
        addInfo("status", new Icon("fas fa-exclamation"), message).setTextColor(Color.RED);
      }
      return this;
    }

    @NotNull
    NotificationInfoLineBuilder addInfo(@NotNull String key, @Nullable Icon icon, @NotNull String info);

    @NotNull
    NotificationBlockBuilder addEntityInfo(@NotNull BaseEntity entity);

    /**
     * Remove info row
     *
     * @param key - info key
     * @return this
     */
    boolean removeInfo(@NotNull String key);
  }

  interface NotificationInfoLineBuilder {

    @NotNull
    NotificationInfoLineBuilder setTextColor(@Nullable String color);

    @NotNull
    NotificationInfoLineBuilder setRightText(@Nullable String text, @Nullable Icon icon, @Nullable String color);

    @NotNull
    NotificationInfoLineBuilder setTooltip(@Nullable String tooltip);

    @NotNull
    default NotificationInfoLineBuilder setRightText(@Nullable String text) {
      return setRightText(text, null, null);
    }

    @NotNull
    default NotificationInfoLineBuilder setRightText(Status status) {
      setRightText(status.name(), null, status.getColor());
      return this;
    }

    @NotNull
    NotificationButtonBuilder setRightButton(@Nullable Icon buttonIcon, @Nullable String buttonText,
                                             @Nullable UIActionHandler handler);

    @NotNull
    NotificationInfoLineBuilder setRightToggleButton(boolean value, @Nullable UIActionHandler handler);

    @NotNull
    NotificationInfoLineBuilder setRightSettingsButton(@NotNull Icon buttonIcon, @NotNull Consumer<UILayoutBuilder> assembler);

    @NotNull
    default NotificationInfoLineBuilder setRightSettingsButton(@NotNull Consumer<UILayoutBuilder> assembler) {
      return setRightSettingsButton(new Icon("fas fa-ellipsis-vertical"), assembler);
    }

    @NotNull
    NotificationInfoLineBuilder setAsLink(@NotNull BaseEntity entity);

    interface NotificationButtonBuilder {
      NotificationButtonBuilder setConfirmMessage(@Nullable String value);

      NotificationButtonBuilder setDialogBackgroundColor(@Nullable String value);

      NotificationButtonBuilder setDialogTitle(@Nullable String value);

      NotificationButtonBuilder setDialogIcon(@Nullable Icon icon);
    }
  }

  interface ContextUIProgress {

    default ProgressBar createProgressBar(@NotNull String key, boolean dummy) {
      return createProgressBar(key, dummy, null);
    }

    /**
     * Create simple progress bar
     *
     * @param key      - progress bar unique key
     * @param dummy    - is submitted progress on UI
     * @param onCancel - create cancellable progress bar if not null
     * @return progress bar
     */
    ProgressBar createProgressBar(@NotNull String key, boolean dummy, @Nullable Runnable onCancel);

    void update(@NotNull String key, double progress, @Nullable String message, boolean cancellable);

    /**
     * Remove progress bar from UI
     *
     * @param key - progress id
     */
    default void done(@NotNull String key) {
      update(key, 100D, null, false);
    }

    @SneakyThrows
    default <T> T runAndGet(
      @NotNull String progressKey,
      boolean cancellable,
      @NotNull ThrowingFunction<ProgressBar, T, Exception> process,
      @Nullable Consumer<Exception> finallyBlock) {

      ProgressBar progressBar = (progress, message, error) -> update(progressKey, progress, message, cancellable);
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
          try {
            finallyBlock.accept(exception);
          } catch (Exception ignore) {
          }
        }
      }
    }

    @SneakyThrows
    default void run(
      @NotNull String progressKey,
      boolean cancellable,
      @NotNull ThrowingConsumer<ProgressBar, Exception> process,
      @Nullable Consumer<Exception> finallyBlock) {

      runAndGet(progressKey, cancellable, progressBar -> {
        process.accept(progressBar);
        return null;
      }, finallyBlock);
    }
  }

  interface ContextUIMedia {

    void playWebAudio(@NotNull ContentStream audioStream, @Nullable Integer from, @Nullable Integer to);
  }

  interface ContextUIDialog {

    MirrorImageDialog topImageDialog(@NotNull String title, @NotNull String icon, @NotNull String iconColor);

    default void sendConfirmation(@NotNull String key, @NotNull String title, @NotNull Runnable confirmHandler,
                                  @NotNull Collection<String> messages, @Nullable String headerButtonAttachTo) {
      sendConfirmation(key, title, responseType -> {
        if (responseType == DialogResponseType.Accepted) {
          confirmHandler.run();
        }
      }, messages, 0, headerButtonAttachTo);
    }

    /**
     * * Send confirmation message to ui with back handler
     *
     * @param headerButtonAttachTo - if set - attach confirm message to header button
     * @param key                  -
     * @param title                -
     * @param confirmHandler       -
     * @param messages             -
     * @param maxTimeoutInSec      -
     */
    default void sendConfirmation(@NotNull String key, @NotNull String title,
                                  @NotNull Consumer<DialogResponseType> confirmHandler, @NotNull Collection<String> messages,
                                  int maxTimeoutInSec, @Nullable String headerButtonAttachTo) {
      sendDialogRequest(key, title, (responseType, pressedButton, parameters) -> confirmHandler.accept(responseType),
        dialogModel -> {
          List<ActionInputParameter> inputs =
            messages.stream().map(ActionInputParameter::message).collect(Collectors.toList());
          dialogModel.headerButtonAttachTo(headerButtonAttachTo).submitButton("Confirm", button -> {
          }).group("General", inputs);
        });
    }

    /**
     * Send request dialog to ui
     *
     * @param dialogModel -
     */
    void sendDialogRequest(@NotNull DialogModel dialogModel);

    /**
     * Send remove dialog request to ui if dialog not need anymore
     */
    void removeDialogRequest(@NotNull String uuid);

    default void sendDialogRequest(@NotNull String key, @NotNull String title, @NotNull DialogRequestHandler actionHandler,
                                   @NotNull Consumer<DialogModel> dialogBuilderSupplier) {
      DialogModel dialogModel = new DialogModel(key, title, actionHandler);
      dialogBuilderSupplier.accept(dialogModel);
      sendDialogRequest(dialogModel);
    }

    /**
     * Request to reload window to UI
     *
     * @param reason          -
     * @param timeoutToReload - timeout to reload. Range 5..60 seconds
     */
    void reloadWindow(@NotNull String reason, int timeoutToReload);

    default void reloadWindow(@NotNull String reason) {
      reloadWindow(reason, 5);
    }

    interface MirrorImageDialog {
      void sendImage(String imageBase64);
    }
  }

  interface ContextUINotification {

    /**
     * Remove notification block if it has no rows anymore
     *
     * @param key - block id
     */
    void removeEmptyBlock(@NotNull String key);

    void addBlock(@NotNull String key, @NotNull String name, @Nullable Icon icon,
                  @Nullable Consumer<NotificationBlockBuilder> builder);

    default void addOrUpdateBlock(@NotNull String key, @NotNull String name, @Nullable Icon icon,
                                  @NotNull Consumer<NotificationBlockBuilder> builder) {
      if (isHasBlock(key)) {
        updateBlock(key, builder);
      } else {
        addBlock(key, name, icon, builder);
      }
    }

    default void addBlockOptional(@NotNull String key, @NotNull String name, @Nullable Icon icon) {
      if (!isHasBlock(key)) {
        addBlock(key, name, icon, null);
      }
    }

    default void updateBlock(@NotNull String key, @NotNull BaseEntity entity) {
      updateBlock(key, builder -> builder.addEntityInfo(entity));
    }

    void updateBlock(@NotNull String key, @NotNull Consumer<NotificationBlockBuilder> builder);

    boolean isHasBlock(@NotNull String key);

    void removeBlock(@NotNull String key);

  }

  interface ContextUIToastr {

    /**
     * Show error toastr message to ui
     *
     * @param message -
     */
    default void error(@NotNull String message) {
      error(null, message, null, null);
    }

    /**
     * Show error toastr message to ui
     *
     * @param ex -
     */
    default void error(@NotNull Exception ex) {
      error(null, null, null, ex);
    }

    /**
     * Show error toastr message to ui
     *
     * @param ex      -
     * @param message -
     */
    default void error(@NotNull String message, @NotNull Exception ex) {
      error(null, message, null, ex);
    }

    /**
     * Show error toastr message to ui
     *
     * @param message -
     * @param title   -
     */
    default void error(@NotNull String title, @NotNull String message) {
      error(title, message, null, null);
    }

    default void error(@NotNull String title, @NotNull String message, @NotNull Exception ex) {
      error(title, message, null, ex);
    }

    default void error(@NotNull String message, @NotNull FlowMap messageParam, @NotNull Exception ex) {
      error(null, message, messageParam, ex);
    }

    default void error(@NotNull String message, @NotNull FlowMap messageParam) {
      error(null, message, messageParam, null);
    }

    default void error(@Nullable String title, @Nullable String message, @Nullable FlowMap messageParam,
                       @Nullable Exception ex) {
      sendMessage(title, message, NotificationLevel.error, messageParam, ex, null);
    }

    default void info(@NotNull String message) {
      info(null, message, null);
    }

    default void info(@NotNull String title, @NotNull String message) {
      info(title, message, null);
    }

    default void info(@NotNull String message, @NotNull FlowMap messageParam) {
      info(null, message, messageParam);
    }

    default void info(@Nullable String title, @NotNull String message, @Nullable FlowMap messageParam) {
      sendMessage(title, message, NotificationLevel.info, messageParam, null, null);
    }

    default void success(@NotNull String message) {
      success(null, message, null);
    }

    default void success(@NotNull String title, @NotNull String message) {
      success(title, message, null);
    }

    default void success(@NotNull String message, @NotNull FlowMap messageParam) {
      success(null, message, messageParam);
    }

    default void success(@Nullable String title, @NotNull String message, @Nullable FlowMap messageParam) {
      sendMessage(title, message, NotificationLevel.success, messageParam, null, null);
    }

    default void warn(@NotNull String message) {
      warn(null, message, null);
    }

    default void warn(@NotNull String title, @NotNull String message) {
      warn(title, message, null);
    }

    default void warn(@NotNull String message, @NotNull FlowMap messageParam) {
      warn(null, message, messageParam);
    }

    default void warn(@Nullable String title, @NotNull String message, @Nullable FlowMap messageParam) {
      sendMessage(title, message, NotificationLevel.warning, messageParam, null, null);
    }

    default void sendMessage(@Nullable String title, @Nullable String message, @Nullable NotificationLevel level,
                             @Nullable FlowMap messageParam, @Nullable Exception ex, @Nullable Integer timeout) {
      title = title == null ? null : Lang.getServerMessage(title, messageParam);
      String text;
      if (ex instanceof ServerException) {
        text = ex.getMessage();
      } else {
        text = StringUtils.isEmpty(message) ? ex == null ? "Unknown error" : ex.getMessage() : message;
        if (text == null) {
          text = getErrorMessage(ex);
        }
        // try cast text to lang
        text = Lang.getServerMessage(text, messageParam);
      }
      sendMessage(title, text, level, timeout);
    }

    void sendMessage(@Nullable String title, @Nullable String message, @Nullable NotificationLevel level, @Nullable Integer timeout);
  }

  interface ContextUIConsole {

    /**
     * Register console plugin name. In case if console plugin available only if some entity is created or not enabled by some case we may show disabled
     * console name on UI
     *
     * @param name - plugin name
     */
    void registerPluginName(@NotNull String name);

    <T extends ConsolePlugin> void registerPlugin(@NotNull String name, @NotNull T plugin);

    default <T extends ConsolePlugin> void registerPlugin(@NotNull T plugin) {
      registerPlugin(plugin.getEntityID(), plugin);
    }

    @Nullable
    <T extends ConsolePlugin> T getRegisteredPlugin(@NotNull String name);

    boolean unRegisterPlugin(@NotNull String name);

    default <T extends ConsolePlugin> boolean unRegisterPlugin(@NotNull T plugin) {
      return unRegisterPlugin(plugin.getEntityID());
    }

    <T extends ConsolePlugin<?>> void openConsole(@NotNull String name);

    /**
     * Instruct ui to fire reload() function to fetch data from server or send value from plugin
     *
     * @param name - plugin name
     */
    default void refreshPluginContent(@NotNull String name) {

    }

    /**
     * Fires UI console plugin to update it's content from 'value'
     *
     * @param name  - plugin name
     * @param value - new value for UI
     */
    void refreshPluginContent(@NotNull String name, Object value);
  }
}
