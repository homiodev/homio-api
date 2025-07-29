package org.homio.api;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.homio.api.entity.BaseEntity;
import org.homio.api.model.OptionModel;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public interface Context {

  @NotNull
  ContextUser user();

  @NotNull
  ContextMedia media();

  @NotNull
  ContextWidget widget();

  @NotNull
  ContextUI ui();

  @NotNull
  ContextEvent event();

  @NotNull
  ContextInstall install();

  @NotNull
  ContextWorkspace workspace();

  @NotNull
  ContextService service();

  @NotNull
  ContextBGP bgp();

  @NotNull
  ContextSetting setting();

  @NotNull
  ContextVar var();

  @NotNull
  ContextHardware hardware();

  @NotNull
  ContextStorage db();

  @NotNull
  ContextNetwork network();

  /**
   * Get or create new file logger for entity
   *
   * @param baseEntity - log file name owner. File will be created at
   *     logs/entities/entityType/entityID_key.log
   * @param suffix - file name suffix
   * @return file logger
   */
  @NotNull
  FileLogger getFileLogger(@NotNull BaseEntity baseEntity, @NotNull String suffix);

  @NotNull
  List<OptionModel> toOptionModels(@Nullable Collection<? extends BaseEntity> entities);

  @NotNull
  <T> T getBean(@NotNull String beanName, @NotNull Class<T> clazz)
      throws NoSuchBeanDefinitionException;

  @NotNull
  <T> T getBean(@NotNull Class<T> clazz) throws NoSuchBeanDefinitionException;

  default <T> T getBean(@NotNull Class<T> clazz, @NotNull Supplier<T> defaultValueSupplier) {
    try {
      return getBean(clazz);
    } catch (Exception ex) {
      return defaultValueSupplier.get();
    }
  }

  @NotNull
  <T> Collection<T> getBeansOfType(@NotNull Class<T> clazz);

  @NotNull
  <T> Map<String, T> getBeansOfTypeWithBeanName(@NotNull Class<T> clazz);

  @NotNull
  <T> List<Class<? extends T>> getClassesWithAnnotation(
      @NotNull Class<? extends Annotation> annotation);

  @NotNull
  <T> List<Class<? extends T>> getClassesWithParent(@NotNull Class<T> baseClass);

  interface FileLogger {

    void logDebug(@Nullable String message);

    void logInfo(@Nullable String message);

    void logWarn(@Nullable String message);

    void logError(@Nullable String message);

    default void logError(@NotNull Exception ex) {
      logError(CommonUtils.getErrorMessage(ex));
    }

    @NotNull
    InputStream getFileInputStream();

    @NotNull
    String getName();
  }
}
