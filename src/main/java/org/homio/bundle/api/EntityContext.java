package org.homio.bundle.api;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.homio.bundle.api.entity.BaseEntity;
import org.homio.bundle.api.entity.UserEntity;
import org.homio.bundle.api.exception.NotFoundException;
import org.homio.bundle.api.model.HasEntityIdentifier;
import org.homio.bundle.api.repository.AbstractRepository;
import org.homio.bundle.api.workspace.scratch.Scratch3ExtensionBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public interface EntityContext {

    @NotNull EntityContextWidget widget();

    @NotNull EntityContextUI ui();

    @NotNull EntityContextEvent event();

    @NotNull EntityContextUDP udp();

    @NotNull EntityContextBGP bgp();

    @NotNull EntityContextSetting setting();

    @NotNull EntityContextVar var();

    /**
     * Register custom Scratch3Extension
     */
    void registerScratch3Extension(@NotNull Scratch3ExtensionBlocks scratch3ExtensionBlocks);

    @Nullable
    default <T extends BaseEntity> T getEntity(@NotNull String entityID) {
        return getEntity(entityID, true);
    }

    @NotNull
    default <T extends BaseEntity> T getEntityRequire(@NotNull String entityID) {
        T entity = getEntity(entityID, true);
        if (entity == null) {
            throw new NotFoundException("Unable to find entity: " + entityID);
        }
        return entity;
    }

    @Nullable
    default <T extends BaseEntity> T getEntityOrDefault(@NotNull String entityID, @Nullable T defEntity) {
        T entity = getEntity(entityID, true);
        return entity == null ? defEntity : entity;
    }

    /**
     * Get entity by entityID.
     */
    @Nullable <T extends BaseEntity> T getEntity(@NotNull String entityID, boolean useCache);

    default Optional<AbstractRepository> getRepository(@NotNull BaseEntity baseEntity) {
        return getRepository(baseEntity.getEntityID());
    }

    Optional<AbstractRepository> getRepository(@NotNull String entityID);

    @NotNull AbstractRepository getRepository(@NotNull Class<? extends BaseEntity> entityClass) throws NotFoundException;

    @Nullable
    default <T extends BaseEntity> T getEntity(@NotNull T entity) {
        return getEntity(entity.getEntityID());
    }

    <T extends HasEntityIdentifier> void createDelayed(@NotNull T entity);

    <T extends HasEntityIdentifier> void updateDelayed(@NotNull T entity, @NotNull Consumer<T> fieldUpdateConsumer);

    @NotNull <T extends HasEntityIdentifier> void save(@NotNull T entity);

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

    @Nullable BaseEntity<? extends BaseEntity> delete(@NotNull String entityId);

    @Nullable AbstractRepository<? extends BaseEntity> getRepositoryByPrefix(@NotNull String repositoryPrefix);

    @Nullable <T extends BaseEntity> T getEntityByName(@NotNull String name, @NotNull Class<T> entityClass);

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

    @NotNull <T> Map<String, Collection<T>> getBeansOfTypeByBundles(@NotNull Class<T> clazz);

    default boolean isAdminUserOrNone() {
        UserEntity user = getUser(false);
        return user == null || user.isAdmin();
    }

    @NotNull
    default UserEntity getUserRequire() {
        UserEntity user = getUser(false);
        if (user == null) {
            throw new NotFoundException("Unable to find authenticated user");
        }
        return user;
    }

    @Nullable
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

    @NotNull Collection<AbstractRepository> getRepositories();

    @NotNull <T> List<Class<? extends T>> getClassesWithAnnotation(@NotNull Class<? extends Annotation> annotation);

    @NotNull <T> List<Class<? extends T>> getClassesWithParent(@NotNull Class<T> baseClass);

    @Nullable
    default String getEnv(@NotNull String key) {
        return getEnv(key, String.class, null);
    }

    @NotNull
    default String getEnv(@NotNull String key, @Nullable String defaultValue) {
        return getEnv(key, String.class, defaultValue);
    }

    @Nullable <T> T getEnv(@NotNull String key, @NotNull Class<T> classType, @Nullable T defaultValue);
}
