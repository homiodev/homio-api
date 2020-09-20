package org.touchhome.bundle.api;

import com.pivovarit.function.ThrowingRunnable;
import com.pivovarit.function.ThrowingSupplier;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.touchhome.bundle.api.json.NotificationEntityJSON;
import org.touchhome.bundle.api.model.BaseEntity;
import org.touchhome.bundle.api.model.HasIdIdentifier;
import org.touchhome.bundle.api.model.UserEntity;
import org.touchhome.bundle.api.repository.AbstractRepository;
import org.touchhome.bundle.api.setting.BundleSettingPlugin;
import org.touchhome.bundle.api.setting.BundleSettingPluginButton;
import org.touchhome.bundle.api.util.NotificationType;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import javax.validation.constraints.NotNull;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface EntityContext {

    String APP_ID = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());

    static boolean isDevEnvironment() {
        return "true".equals(System.getProperty("development"));
    }

    static boolean isDockerEnvironment() {
        return "true".equals(System.getProperty("docker"));
    }

    static boolean isLinuxEnvironment() {
        return SystemUtils.IS_OS_LINUX && !isDockerEnvironment() && !isDevEnvironment();
    }

    static boolean isLinuxOrDockerEnvironment() {
        return SystemUtils.IS_OS_LINUX && !isDevEnvironment();
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

    void removeHeaderNotification(NotificationEntityJSON notificationEntityJSON);

    <T> T getSettingValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass);

    <T> String getSettingRawValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass);

    default <T> T getSettingValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, T defaultValue) {
        T value = getSettingValue(settingClass);
        return value == null ? defaultValue : value;
    }

    default <T> void listenSettingValueAsync(@ApiParam("setting class") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("unique key") String key, @ApiParam("listener") Consumer<T> listener) {
        listenSettingValue(settingClass, key, value ->
                new Thread(() -> listener.accept(value), "run-listen-value-async-" + settingClass.getSimpleName()).start());
    }

    default <T> void listenSettingValueAsync(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("unique key") String key, @ApiParam("listener") Runnable listener) {
        listenSettingValueAsync(settingClass, key, t -> listener.run());
    }

    default <T> void listenSettingValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("unique key") String key, @ApiParam("listener") Runnable listener) {
        listenSettingValue(settingClass, key, p -> listener.run());
    }

    <T> void listenSettingValue(Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("unique key") String key, @ApiParam("listener") Consumer<T> listener);

    <T> void setSettingValueRaw(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> bundleSettingPluginClazz, @ApiParam("value") @NotNull String value);

    <T> void setSettingValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("value") @NotNull T value);

    /**
     * Save setting value without firing events
     *
     * @return value converted to string
     */
    <T> String setSettingValueSilence(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("value") @NotNull T value);

    <T> void setSettingValueSilenceRaw(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("value") @NotNull String value);

    default <T extends BaseEntity> T getEntity(@ApiParam("entityID") String entityID) {
        return getEntity(entityID, true);
    }

    default <T extends BaseEntity> T getEntityOrDefault(@ApiParam("entityID") String entityID, @ApiParam("defEntity") T defEntity) {
        T entity = getEntity(entityID, true);
        return entity == null ? defEntity : entity;
    }

    <T extends BaseEntity> T getEntity(@ApiParam("entityID") String entityID, @ApiParam("useCache") boolean useCache);

    default Optional<AbstractRepository> getRepository(@ApiParam("baseEntity") BaseEntity baseEntity) {
        return getRepository(baseEntity.getEntityID());
    }

    Optional<AbstractRepository> getRepository(@ApiParam("entityID") String entityID);

    AbstractRepository getRepository(Class<? extends BaseEntity> entityClass);

    default <T extends BaseEntity> T getEntity(@ApiParam("entity") T entity) {
        return getEntity(entity.getEntityID());
    }

    <T extends HasIdIdentifier> void saveDelayed(@ApiParam("entity") T entity);

    <T extends BaseEntity> void saveDelayed(@ApiParam("entity") T entity);

    <T extends HasIdIdentifier> void save(@ApiParam("entity") T entity);

    <T extends BaseEntity> T save(@ApiParam("entity") T entity);

    default <T extends BaseEntity> T delete(@ApiParam("entity") T entity) {
        return (T) delete(entity.getEntityID());
    }

    default void sendInfoMessage(@ApiParam("message") String message) {
        sendNotification(NotificationEntityJSON.info("info-" + message.hashCode()).setName(message));
    }

    default void sendErrorMessage(@ApiParam("message") String message, @ApiParam("ex") Exception ex) {
        sendNotification(NotificationEntityJSON.danger("error-" + message.hashCode())
                .setName(message + ". Cause: " + TouchHomeUtils.getErrorMessage(ex)));
    }

    <T extends BaseEntity> List<T> findAll(@ApiParam("clazz") Class<T> clazz);

    <T extends BaseEntity> List<T> findAllByPrefix(@ApiParam("prefix") String prefix);

    default <T extends BaseEntity> List<T> findAll(@ApiParam("entity") T entity) {
        return (List<T>) findAll(entity.getClass());
    }

    BaseEntity<? extends BaseEntity> delete(@ApiParam("entityID") String entityId);

    AbstractRepository<? extends BaseEntity> getRepositoryByPrefix(@ApiParam("repositoryPrefix") String repositoryPrefix);

    <T extends BaseEntity> T getEntityByName(@ApiParam("name") String name, @ApiParam("entityClass") Class<T> entityClass);

    default <T extends BaseEntity> void addEntityUpdateListener(@ApiParam("entityID") String entityID, @ApiParam("listener") Consumer<T> listener) {
        this.addEntityUpdateListener(entityID, (t, t2) -> listener.accept((T) t));
    }

    <T extends BaseEntity> void addEntityUpdateListener(@ApiParam("entityID") String entityID, @ApiParam("listener") BiConsumer<T, T> listener);

    /**
     * Listen any changes fot BaseEntity of concrete type.
     *
     * @param entityClass type to listen
     * @param listener    handler invoke when entity update
     */
    default <T extends BaseEntity> void addEntityUpdateListener(@ApiParam("entityClass") Class<T> entityClass, @ApiParam("listener") Consumer<T> listener) {
        this.addEntityUpdateListener(entityClass, (t, t2) -> listener.accept(t));
    }

    /**
     * Listen any changes fot BaseEntity of concrete type.
     *
     * @param entityClass type to listen
     * @param listener    handler invoke when entity update. OldValue/NewValue
     */
    <T extends BaseEntity> void addEntityUpdateListener(@ApiParam("entityClass") Class<T> entityClass, @ApiParam("listener") BiConsumer<T, T> listener);

    <T extends BaseEntity> void addEntityRemovedListener(@ApiParam("entityClass") Class<T> entityClass, @ApiParam("listener") Consumer<T> listener);

    <T extends BaseEntity> void removeEntityUpdateListener(@ApiParam("entityID") String entityID, @ApiParam("listener") BiConsumer<T, T> listener);

    void setFeatureState(@ApiParam("feature") String feature, @ApiParam("state") boolean state);

    boolean isFeatureEnabled(@ApiParam("deviceFeature") String deviceFeature);

    Map<String, Boolean> getDeviceFeatures();

    <T> T getBean(@ApiParam("beanName") String beanName, @ApiParam("clazz") Class<T> clazz);

    <T> T getBean(@ApiParam("clazz") Class<T> clazz);

    <T> Collection<T> getBeansOfType(@ApiParam("clazz") Class<T> clazz);

    <T> Map<String, T> getBeansOfTypeWithBeanName(@ApiParam("clazz") Class<T> clazz);

    <T> Map<String, Collection<T>> getBeansOfTypeByBundles(@ApiParam("clazz") Class<T> clazz);

    default UserEntity getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return getEntity((String) authentication.getCredentials());
        }
        return null;
    }

    Collection<AbstractRepository> getRepositories();

    <T> List<Class<? extends T>> getClassesWithAnnotation(@ApiParam("annotation") Class<? extends Annotation> annotation);

    ThreadContext<Void> schedule(String name, int timeout, TimeUnit timeUnit, ThrowingRunnable<Exception> command, boolean showOnUI);

    default ThreadContext<Void> run(String name, ThrowingRunnable<Exception> command, boolean showOnUI) {
        return run(name, () -> {
            command.run();
            return null;
        }, showOnUI);
    }

    <T> ThreadContext<T> run(String name, ThrowingSupplier<T, Exception> command, boolean showOnUI);

    boolean isThreadExists(String name);

    void cancelThread(String name);

    interface ThreadContext<T> {
        String getName();

        String getState();

        void setState(String state);

        String getDescription();

        void setDescription(String description);

        boolean isStopped();

        void cancel();

        T await(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException;

        void onError(Consumer<Exception> errorListener);
    }
}
