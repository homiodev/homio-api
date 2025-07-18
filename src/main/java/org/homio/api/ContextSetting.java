package org.homio.api;

import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingRunnable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.HasJsonData;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.model.Status;
import org.homio.api.setting.SettingPlugin;
import org.homio.api.setting.SettingPluginOptions;
import org.homio.api.setting.console.header.dynamic.DynamicConsoleHeaderContainerSettingPlugin;
import org.homio.api.setting.console.header.dynamic.DynamicConsoleHeaderSettingPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContextSetting {

  int SERVER_PORT = Integer.getInteger("server.port", 9111);
  boolean IS_DEV_ENVIRONMENT = Boolean.getBoolean("development");
  boolean IS_DOCKER_ENVIRONMENT = Boolean.getBoolean("docker");

  AtomicReference<MemSetterHandler> MEM_HANDLER = new AtomicReference<>();

  static <T> T getMemValue(
      @NotNull HasEntityIdentifier entity,
      @NotNull String distinguishKey,
      @Nullable T defaultValue) {
    return (T) MEM_HANDLER.get().getValue(entity, distinguishKey, defaultValue);
  }

  /**
   * Set some value in memory and associate it with entity
   *
   * @param entity - entity to associate with
   * @param distinguishKey - unqiue key for stored value that associanted with entity
   * @param title - uses to log updated value to console
   * @param value - value to store. remove from map if null
   * @param <T> -
   */
  static <T> void setMemValue(
      @NotNull HasEntityIdentifier entity,
      @NotNull String distinguishKey,
      @NotNull String title,
      @Nullable T value) {
    MEM_HANDLER.get().setValue(entity, distinguishKey, title, value);
  }

  static Status getStatus(
      @NotNull HasEntityIdentifier entity,
      @NotNull String distinguishKey,
      @Nullable Status defaultStatus) {
    return getMemValue(entity, distinguishKey, defaultStatus);
  }

  static void setStatus(
      @NotNull HasEntityIdentifier entity,
      @NotNull String distinguishKey,
      @NotNull String title,
      @Nullable Status status) {
    setMemValue(entity, distinguishKey, title, status);
  }

  static void setStatus(
      @NotNull HasEntityIdentifier entity,
      @NotNull String distinguishKey,
      @NotNull String title,
      @Nullable Status status,
      String message) {
    setMemValue(entity, distinguishKey + "Message", "", message);
    setMemValue(entity, distinguishKey, title, status);
  }

  static String getMessage(@NotNull HasEntityIdentifier entity, @NotNull String distinguishKey) {
    return getMemValue(entity, distinguishKey + "Message", null);
  }

  static void setMessage(
      @NotNull HasEntityIdentifier entity,
      @NotNull String distinguishKey,
      @Nullable String message) {
    setMemValue(entity, distinguishKey + "Message", "", message);
  }

  /**
   * Get unmodifiable list of all available places. Configured via settings
   *
   * @return list of places
   */
  List<String> getPlaces();

  /**
   * Update setting components on ui
   *
   * @param settingPlugin - setting to reload
   */
  void reloadSettings(@NotNull Class<? extends SettingPluginOptions> settingPlugin);

  void reloadDynamicSettings(
      @NotNull
          Class<? extends DynamicConsoleHeaderContainerSettingPlugin> dynamicSettingPluginClass);

  /**
   * Get setting value by class name
   *
   * @param settingClass - setting
   * @param <T> -
   * @return setting value
   */
  <T> T getValue(@NotNull Class<? extends SettingPlugin<T>> settingClass);

  /**
   * Get unparsed setting value by class name
   *
   * @param settingClass - setting
   * @param <T> -
   * @return raw value
   */
  <T> String getRawValue(@NotNull Class<? extends SettingPlugin<T>> settingClass);

  <T> BaseEntity createDynamicSetting(
      /*@NotNull DynamicConsoleHeaderContainerSettingPlugin dynamicSettingPluginClass,*/
      @NotNull DynamicConsoleHeaderSettingPlugin<T> dynamicSetting);

  /**
   * Get setting value by class name or default value if null
   *
   * @param settingClass - setting
   * @param defaultValue - def value
   * @param <T> -
   * @return value
   */
  default <T> T getValue(
      @NotNull Class<? extends SettingPlugin<T>> settingClass, @Nullable T defaultValue) {
    T value = getValue(settingClass);
    return value == null ? defaultValue : value;
  }

  default <T> void listenValue(
      @NotNull Class<? extends SettingPlugin<T>> settingClass,
      @NotNull String key,
      @NotNull ThrowingRunnable<Exception> listener) {
    listenValue(settingClass, key, p -> listener.run());
  }

  /**
   * Usually listeners executes in separate thread but in some cases we need access to user who
   * updating value and we need run listener inside http request
   *
   * @param listener -
   * @param key -
   * @param settingClass -
   * @param <T> -
   */
  <T> void listenValueInRequest(
      @NotNull Class<? extends SettingPlugin<T>> settingClass,
      @NotNull String key,
      @NotNull ThrowingConsumer<T, Exception> listener);

  <T> void listenValue(
      @NotNull Class<? extends SettingPlugin<T>> settingClass,
      @NotNull String key,
      @NotNull ThrowingConsumer<T, Exception> listener);

  <T> void unListenValue(
      @NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull String key);

  default <T> void listenValueAndGet(
      @NotNull Class<? extends SettingPlugin<T>> settingClass,
      @NotNull String key,
      @NotNull ThrowingConsumer<T, Exception> listener)
      throws Exception {
    listenValue(settingClass, key, listener);
    listener.accept(getValue(settingClass));
  }

  /**
   * Listen for changes inside BaseEntity and fire listener every update
   *
   * @param entity - source entity
   * @param jsonKey - json key
   * @param typeClass - target type
   * @param listener - listener
   * @param <T> - entity type
   * @param <P> - target type
   */
  <T extends BaseEntity & HasJsonData, P> void listenEntityValueAndGet(
      @NotNull T entity,
      @NotNull String key,
      @Nullable String jsonKey,
      @NotNull Class<P> typeClass,
      @NotNull ThrowingConsumer<P, Exception> listener);

  <T> void setValueRaw(
      @NotNull Class<? extends SettingPlugin<T>> settingPluginClazz, @Nullable String value);

  <T> void setValue(@NotNull Class<? extends SettingPlugin<T>> settingClass, @Nullable T value);

  /**
   * Save setting value without firing events
   *
   * @param value -
   * @param settingClass -
   * @param <T> -
   * @return value converted to string
   */
  <T> String setValueSilence(
      @NotNull Class<? extends SettingPlugin<T>> settingClass, @Nullable T value);

  <T> void setValueSilenceRaw(
      @NotNull Class<? extends SettingPlugin<T>> settingClass, @Nullable String value);

  default @NotNull String getEnvRequire(@NotNull String key) {
    return Objects.requireNonNull(getEnv(key));
  }

  default @Nullable String getEnv(@NotNull String key) {
    return getEnv(key, String.class, null, false, null);
  }

  default @Nullable String getEnv(
      @NotNull String key, @Nullable String defaultValue, boolean store) {
    return getEnv(key, String.class, defaultValue, store, null);
  }

  default @NotNull <T> T getEnvRequire(
      @NotNull String key, @NotNull Class<T> classType, @NotNull T defaultValue, boolean store) {
    return Objects.requireNonNull(getEnv(key, classType, defaultValue, store, null));
  }

  @Nullable
  <T> T getEnv(
      @NotNull String key,
      @NotNull Class<T> classType,
      @Nullable T defaultValue,
      boolean store,
      @Nullable String description);

  default int getEnv(@NotNull String key, int defaultValue, boolean store) {
    return getEnv(key, defaultValue, store, null);
  }

  default int getEnv(
      @NotNull String key, int defaultValue, boolean store, @Nullable String description) {
    Integer value = getEnv(key, Integer.class, defaultValue, store, description);
    return value == null ? defaultValue : value;
  }

  default boolean getEnv(@NotNull String key, boolean defaultValue, boolean store) {
    return getEnv(key, defaultValue, store, null);
  }

  default boolean getEnv(
      @NotNull String key, boolean defaultValue, boolean store, @Nullable String description) {
    Boolean value = getEnv(key, Boolean.class, defaultValue, store, description);
    return value == null ? defaultValue : value;
  }

  void setEnv(@NotNull String key, @NotNull Object value);

  default boolean hasProfile(@NotNull String key) {
    String profileSetting = getEnv("spring.profiles.active");
    return Objects.toString(profileSetting, "").contains(key);
  }

  @NotNull
  String getApplicationVersion();

  default int getApplicationMajorVersion() {
    return Integer.parseInt(getApplicationVersion().substring(1).split("\\.")[0]);
  }

  interface MemSetterHandler {

    void setValue(
        @NotNull HasEntityIdentifier entity,
        @NotNull String key,
        @NotNull String title,
        @Nullable Object value);

    Object getValue(
        @NotNull HasEntityIdentifier entity, @NotNull String key, @Nullable Object defaultValue);
  }
}
