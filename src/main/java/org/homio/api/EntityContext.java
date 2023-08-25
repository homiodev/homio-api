package org.homio.api;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.UserEntity;
import org.homio.api.exception.NotFoundException;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

public interface EntityContext {

    @NotNull EntityContextMedia media();

    @NotNull EntityContextWidget widget();

    @NotNull EntityContextUI ui();

    @NotNull EntityContextEvent event();

    @NotNull EntityContextInstall install();

    @NotNull EntityContextWorkspace workspace();

    @NotNull EntityContextService service();

    @NotNull EntityContextBGP bgp();

    @NotNull EntityContextSetting setting();

    @NotNull EntityContextVar var();

    @NotNull EntityContextHardware hardware();

    @NotNull EntityContextStorage storage();

    @Nullable
    default <T extends BaseEntity> T getEntity(@NotNull String entityID) {
        return getEntity(entityID, true);
    }

    @Nullable
    default <T extends BaseEntity> T getEntity(Class<T> entityClass, @NotNull String entityID) {
        T entity = CommonUtils.newInstance(entityClass);
        return getEntity(entity.setEntityID(entityID), true);
    }

    @NotNull
    default <T extends BaseEntity> T getEntityRequire(@NotNull String entityID) {
        T entity = getEntity(entityID, true);
        if (entity == null) {
            throw new NotFoundException("Unable to find entity: " + entityID);
        }
        return entity;
    }

    @NotNull
    default <T extends BaseEntity> T getEntityRequire(Class<T> entityClass, @NotNull String entityID) {
        T entity = CommonUtils.newInstance(entityClass);
        return getEntityRequire(entity.setEntityID(entityID));
    }

    @Nullable
    default <T extends BaseEntity> T getEntityOrDefault(@NotNull String entityID, @Nullable T defEntity) {
        T entity = getEntity(entityID, true);
        return entity == null ? defEntity : entity;
    }

    /**
     * Get entity by entityID.
     *
     * @param entityID - entity unique id to fetch
     * @param useCache - allow to use cache or direct db
     * @param <T>      -
     * @return base entity
     */
    @Nullable <T extends BaseEntity> T getEntity(@NotNull String entityID, boolean useCache);

    @Nullable
    default <T extends BaseEntity> T getEntity(@NotNull T entity) {
        return getEntity(entity.getEntityID());
    }

    <T extends BaseEntity> void createDelayed(@NotNull T entity);

    <T extends BaseEntity> void updateDelayed(@NotNull T entity, @NotNull Consumer<T> fieldUpdateConsumer);

    @NotNull
    default <T extends BaseEntity> T save(@NotNull T entity) {
        return save(entity, true);
    }

    @NotNull <T extends BaseEntity> T save(@NotNull T entity, boolean fireNotifyListeners);

    @Nullable
    default <T extends BaseEntity> T delete(@NotNull T entity) {
        return (T) delete(entity.getEntityID());
    }

    @Nullable
    default <T extends BaseEntity> T findAny(@NotNull Class<T> clazz) {
        List<T> list = findAll(clazz);
        return list.isEmpty() ? null : list.iterator().next();
    }

    @NotNull <T extends BaseEntity> List<T> findAll(@NotNull Class<T> clazz);

    @NotNull <T extends BaseEntity> List<T> findAllByPrefix(@NotNull String prefix);

    @NotNull
    default <T extends BaseEntity> List<T> findAll(@NotNull T entity) {
        return (List<T>) findAll(entity.getClass());
    }

    @Nullable BaseEntity delete(@NotNull String entityId);

    @NotNull <T> T getBean(@NotNull String beanName, @NotNull Class<T> clazz) throws NoSuchBeanDefinitionException;

    @NotNull <T> T getBean(@NotNull Class<T> clazz) throws NoSuchBeanDefinitionException;

    default <T> T getBean(@NotNull Class<T> clazz, @NotNull Supplier<T> defaultValueSupplier) {
        try {
            return getBean(clazz);
        } catch (Exception ex) {
            return defaultValueSupplier.get();
        }
    }

    @NotNull <T> Collection<T> getBeansOfType(@NotNull Class<T> clazz);

    @NotNull <T> Map<String, T> getBeansOfTypeWithBeanName(@NotNull Class<T> clazz);

    @NotNull <T> Map<String, Collection<T>> getBeansOfTypeByAddons(@NotNull Class<T> clazz);

    default boolean isAdmin() {
        UserEntity user = getUser();
        return user != null && user.isAdmin();
    }

    @SneakyThrows
    default void assertAdminAccess() {
        if (!isAdmin()) {
            throw new IllegalAccessException();
        }
    }

    @NotNull
    default UserEntity getUserRequire() {
        UserEntity user = getUser();
        if (user == null) {
            throw new NotFoundException("Unable to find authenticated user");
        }
        return user;
    }

    default void assertAccess(@NotNull String resource) {
        UserEntity user = getUserRequire();
        if (!user.isAllowResource(resource)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    default boolean accessEnabled(@NotNull String resource) {
        UserEntity user = getUserRequire();
        return user.isAllowResource(resource);
    }

    @Nullable
    default UserEntity getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            User user = (User) authentication.getPrincipal();
            String userEntityID = user.getUsername().split("~~~")[0];
            return getEntity(userEntityID);
        }
        return null;
    }

    void registerResource(String resource);

    @NotNull <T> List<Class<? extends T>> getClassesWithAnnotation(@NotNull Class<? extends Annotation> annotation);

    @NotNull <T> List<Class<? extends T>> getClassesWithParent(@NotNull Class<T> baseClass);
}
