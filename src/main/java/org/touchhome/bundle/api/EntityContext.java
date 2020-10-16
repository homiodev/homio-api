package org.touchhome.bundle.api;

import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.touchhome.bundle.api.model.BaseEntity;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.model.UserEntity;
import org.touchhome.bundle.api.repository.AbstractRepository;

import java.lang.annotation.Annotation;
import java.net.DatagramPacket;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface EntityContext extends NotificationMessageEntityContext, SettingEntityContext, ThreadEntityContext {

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

    <T extends HasEntityIdentifier> void createDelayed(@ApiParam("entity") T entity);

    <T extends HasEntityIdentifier> void updateDelayed(T entity, Consumer<T> fieldUpdateConsumer);

    <T extends HasEntityIdentifier> void save(@ApiParam("entity") T entity);

    <T extends BaseEntity> T save(@ApiParam("entity") T entity);

    default <T extends BaseEntity> T delete(@ApiParam("entity") T entity) {
        return (T) delete(entity.getEntityID());
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

    <T extends BaseEntity> void addEntityRemovedListener(@ApiParam("entityID") String entityID, @ApiParam("listener") Consumer<T> listener);

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

    void listenUdp(String host, int port, BiConsumer<DatagramPacket, String> listener);
}
