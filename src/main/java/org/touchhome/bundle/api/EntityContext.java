package org.touchhome.bundle.api;

import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.UserEntity;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.repository.AbstractRepository;
import org.touchhome.bundle.api.workspace.scratch.Scratch3ExtensionBlocks;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface EntityContext {

    AtomicReference<MemSetterHandler> MEM_HANDLER = new AtomicReference<>();

    interface MemSetterHandler {
        void setValue(String entityID, String key, Object value);

        Object getValue(String entityID, String key, Object defaultValue);
    }

    static Status getStatus(String key, @NotNull String distinguishKey, Status defaultStatus) {
        return (Status) MEM_HANDLER.get().getValue(key, distinguishKey, defaultStatus);
    }

    static void setStatus(String entityId, @NotNull String distinguishKey, Status status) {
        MEM_HANDLER.get().setValue(entityId, distinguishKey, status);
    }

    static String getMessage(String key, @NotNull String distinguishKey) {
        return (String) MEM_HANDLER.get().getValue(key, distinguishKey + "_msg", null);
    }

    static void setMessage(String entityId, @NotNull String distinguishKey, String value) {
        MEM_HANDLER.get().setValue(entityId, distinguishKey + "_msg", value);
    }

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

    EntityContextWidget widget();

    EntityContextUI ui();

    EntityContextEvent event();

    EntityContextUDP udp();

    EntityContextBGP bgp();

    EntityContextSetting setting();

    EntityContextVar var();

    /**
     * Register custom Scratch3Extension
     */
    void registerScratch3Extension(Scratch3ExtensionBlocks scratch3ExtensionBlocks);

    default <T extends BaseEntity> T getEntity(@NotNull String entityID) {
        return getEntity(entityID, true);
    }

    default <T extends BaseEntity> T getEntityOrDefault(@NotNull String entityID, @Nullable T defEntity) {
        T entity = getEntity(entityID, true);
        return entity == null ? defEntity : entity;
    }

    <T extends BaseEntity> T getEntity(@NotNull String entityID, boolean useCache);

    default Optional<AbstractRepository> getRepository(@NotNull BaseEntity baseEntity) {
        return getRepository(baseEntity.getEntityID());
    }

    Optional<AbstractRepository> getRepository(@NotNull String entityID);

    AbstractRepository getRepository(@NotNull Class<? extends BaseEntity> entityClass);

    default <T extends BaseEntity> T getEntity(@NotNull T entity) {
        return getEntity(entity.getEntityID());
    }

    <T extends HasEntityIdentifier> void createDelayed(@NotNull T entity);

    <T extends HasEntityIdentifier> void updateDelayed(@NotNull T entity, @NotNull Consumer<T> fieldUpdateConsumer);

    <T extends HasEntityIdentifier> void save(@NotNull T entity);

    default <T extends BaseEntity> T save(@NotNull T entity) {
        return save(entity, true);
    }

    <T extends BaseEntity> T save(@NotNull T entity, boolean fireNotifyListeners);

    default <T extends BaseEntity> T delete(@NotNull T entity) {
        return (T) delete(entity.getEntityID());
    }

    default <T extends BaseEntity> T findAny(@NotNull Class<T> clazz) {
        List<T> list = findAll(clazz);
        return list.isEmpty() ? null : list.iterator().next();
    }

    <T extends BaseEntity> List<T> findAll(@NotNull Class<T> clazz);

    <T extends BaseEntity> List<T> findAllByPrefix(@NotNull String prefix);

    default <T extends BaseEntity> List<T> findAll(@NotNull T entity) {
        return (List<T>) findAll(entity.getClass());
    }

    BaseEntity<? extends BaseEntity> delete(@NotNull String entityId);

    AbstractRepository<? extends BaseEntity> getRepositoryByPrefix(@NotNull String repositoryPrefix);

    <T extends BaseEntity> T getEntityByName(@NotNull String name, @NotNull Class<T> entityClass);

    void setFeatureState(@NotNull String feature, boolean state);

    boolean isFeatureEnabled(@NotNull String deviceFeature);

    Map<String, Boolean> getDeviceFeatures();

    <T> T getBean(@NotNull String beanName, @NotNull Class<T> clazz);

    <T> T getBean(@NotNull Class<T> clazz);

    default <T> T getBean(@NotNull Class<T> clazz, @NotNull Supplier<T> defaultValueSupplier) {
        try {
            return getBean(clazz);
        } catch (Exception ex) {
            return defaultValueSupplier.get();
        }
    }

    <T> Collection<T> getBeansOfType(@NotNull Class<T> clazz);

    <T> Map<String, T> getBeansOfTypeWithBeanName(@NotNull Class<T> clazz);

    <T> Map<String, Collection<T>> getBeansOfTypeByBundles(@NotNull Class<T> clazz);

    default boolean isAdminUserOrNone() {
        UserEntity user = getUser(false);
        return user == null || user.isAdmin();
    }

    default UserEntity getUser(boolean anonymousIfNotFound) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            UserEntity entity = getEntity((String) authentication.getCredentials());
            if (entity == null && anonymousIfNotFound) {
                entity = UserEntity.ANONYMOUS_USER;
            }
            return entity;
        }
        return null;
    }

    Collection<AbstractRepository> getRepositories();

    <T> List<Class<? extends T>> getClassesWithAnnotation(@NotNull Class<? extends Annotation> annotation);

    <T> List<Class<? extends T>> getClassesWithParent(@NotNull Class<T> baseClass, String... packages);

    default String getEnv(@NotNull String key) {
        return getEnv(key, String.class, null);
    }

    default String getEnv(@NotNull String key, @Nullable String defaultValue) {
        return getEnv(key, String.class, defaultValue);
    }

    <T> T getEnv(@NotNull String key, @NotNull Class<T> classType, @Nullable T defaultValue);

    interface EntityUpdateListener<T> {
        void entityUpdated(T newValue, T oldValue);
    }
}
