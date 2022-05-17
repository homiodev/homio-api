package org.touchhome.bundle.api;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.entity.UserEntity;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.repository.AbstractRepository;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface EntityContext {

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

    default <T extends BaseEntity> T getEntity(String entityID) {
        return getEntity(entityID, true);
    }

    default <T extends BaseEntity> T getEntityOrDefault(String entityID, T defEntity) {
        T entity = getEntity(entityID, true);
        return entity == null ? defEntity : entity;
    }

    <T extends BaseEntity> T getEntity(String entityID, boolean useCache);

    default Optional<AbstractRepository> getRepository(BaseEntity baseEntity) {
        return getRepository(baseEntity.getEntityID());
    }

    Optional<AbstractRepository> getRepository(String entityID);

    AbstractRepository getRepository(Class<? extends BaseEntity> entityClass);

    default <T extends BaseEntity> T getEntity(T entity) {
        return getEntity(entity.getEntityID());
    }

    <T extends HasEntityIdentifier> void createDelayed(T entity);

    <T extends HasEntityIdentifier> void updateDelayed(T entity, Consumer<T> fieldUpdateConsumer);

    <T extends HasEntityIdentifier> void save(T entity);

    <T extends BaseEntity> T save(T entity);

    default <T extends BaseEntity> T delete(T entity) {
        return (T) delete(entity.getEntityID());
    }

    default <T extends BaseEntity> T findAny(Class<T> clazz) {
        List<T> list = findAll(clazz);
        return list.isEmpty() ? null : list.iterator().next();
    }

    <T extends BaseEntity> List<T> findAll(Class<T> clazz);

    <T extends BaseEntity> List<T> findAllByPrefix(String prefix);

    default <T extends BaseEntity> List<T> findAll(T entity) {
        return (List<T>) findAll(entity.getClass());
    }

    BaseEntity<? extends BaseEntity> delete(String entityId);

    AbstractRepository<? extends BaseEntity> getRepositoryByPrefix(String repositoryPrefix);

    <T extends BaseEntity> T getEntityByName(String name, Class<T> entityClass);

    void setFeatureState(String feature, boolean state);

    boolean isFeatureEnabled(String deviceFeature);

    Map<String, Boolean> getDeviceFeatures();

    <T> T getBean(String beanName, Class<T> clazz);

    <T> T getBean(Class<T> clazz);

    default <T> T getBean(Class<T> clazz, Supplier<T> defaultValueSupplier) {
        try {
            return getBean(clazz);
        } catch (Exception ex) {
            return defaultValueSupplier.get();
        }
    }

    <T> Collection<T> getBeansOfType(Class<T> clazz);

    <T> Map<String, T> getBeansOfTypeWithBeanName(Class<T> clazz);

    <T> Map<String, Collection<T>> getBeansOfTypeByBundles(Class<T> clazz);

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

    <T> List<Class<? extends T>> getClassesWithAnnotation(Class<? extends Annotation> annotation);

    <T> List<Class<? extends T>> getClassesWithParent(Class<T> baseClass, String... packages);

    default String getEnv(String key) {
        return getEnv(key, String.class, null);
    }

    default String getEnv(String key, String defaultValue) {
        return getEnv(key, String.class, defaultValue);
    }

    <T> T getEnv(String key, Class<T> classType, T defaultValue);

    interface EntityUpdateListener<T> {
        void entityUpdated(T newValue, T oldValue);
    }
}
