package org.homio.api.workspace;

import static org.homio.api.entity.HasJsonData.LIST_DELIMITER;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingRunnable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.SneakyThrows;
import org.homio.api.Context;
import org.homio.api.ContextBGP;
import org.homio.api.entity.BaseEntity;
import org.homio.api.exception.ServerException;
import org.homio.api.service.EntityService;
import org.homio.api.state.RawType;
import org.homio.api.state.State;
import org.homio.api.util.CommonUtils;
import org.homio.api.util.SpringUtils;
import org.homio.api.workspace.scratch.MenuBlock;
import org.homio.hquery.Curl;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.Resource;

public interface WorkspaceBlock {

  Set<String> MEDIA_EXTENSIONS =
      new HashSet<>(
          Arrays.asList(
              ".jpg", ".jpeg", ".png", ".gif", ".jpe", ".jif", ".jfif", ".jfi", ".webp", ".webm",
              ".mkv", ".flv", ".vob", ".ogv", ".ogg", ".drc", ".avi", ".wmv", ".mp4", ".mpg",
              ".mpeg", ".m4v", ".flv", ".xlsx", ".xltx", ".xls", ".xlt", ".xml", ".json", ".txt",
              ".csv", ".pdf", ".htm", ".html", ".7z", ".zip", ".tar.gz", ".gz", ".js", ".mp3"));

  @SneakyThrows
  static String evalStringWithContext(String value, Function<String, String> valueSupplier) {
    value =
        SpringUtils.replaceEnvValues(
            value,
            (text, defValue, prefix) -> {
              text = text.toUpperCase();
              switch (text) {
                case "TIMESTAMP":
                  return String.valueOf(System.currentTimeMillis());
                case "DATETIME":
                  return new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").format(new Date());
                case "DATE":
                  return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                case "TIME":
                  return new SimpleDateFormat("HH-mm-ss").format(new Date());
                case "HOUR":
                  return new SimpleDateFormat("HH").format(new Date());
                case "MIN":
                  return new SimpleDateFormat("mm").format(new Date());
                case "SEC":
                  return new SimpleDateFormat("ss").format(new Date());
                case "YEAR":
                  return new SimpleDateFormat("yyyy").format(new Date());
                case "MONTH":
                  return new SimpleDateFormat("MM").format(new Date());
                case "DAY":
                  return new SimpleDateFormat("dd").format(new Date());
                case "UUID":
                  return UUID.randomUUID().toString();
                case "FILES":
                  Path dir = Files.createDirectories(Paths.get(prefix.toString()));
                  return String.valueOf(Objects.requireNonNull(dir.toFile().list()).length);
              }
              return Objects.toString(valueSupplier.apply(text), defValue);
            });

    DoubleEvaluator eval = new DoubleEvaluator();
    value =
        SpringUtils.replaceHashValues(
            value,
            (text, defValue, prefix) -> {
              try {
                return String.valueOf(eval.evaluate(text));
              } catch (Exception ignore) {
                return Objects.toString(defValue, text);
              }
            });
    return value;
  }

  void logError(String message, Object... params);

  void logErrorAndThrow(String message, Object... params) throws ServerException;

  void logWarn(String message, Object... params);

  void logInfo(String message, Object... params);

  <P> P getMenuValue(String key, MenuBlock menuBlock, Class<P> type);

  Resource getFile(String key, MenuBlock menuBlock, boolean required);

  default <P> List<P> getMenuValues(String key, MenuBlock menuBlock, Class<P> type) {
    return getMenuValues(key, menuBlock, type, LIST_DELIMITER);
  }

  <P> List<P> getMenuValues(String key, MenuBlock menuBlock, Class<P> type, String delimiter);

  default String getMenuValue(String key, MenuBlock.ServerMenuBlock menuBlock) {
    return getMenuValue(key, menuBlock, String.class);
  }

  default <T extends BaseEntity> T getMenuValueEntity(
      String key, MenuBlock.ServerMenuBlock menuBlock) {
    return context().db().get(getMenuValue(key, menuBlock, String.class));
  }

  default <S> S getEntityService(
      String key, MenuBlock.ServerMenuBlock menuBlock, Class<S> serviceClass) {
    BaseEntity baseEntity = getMenuValueEntityRequired(key, menuBlock);
    if (!(baseEntity instanceof EntityService)) {
      logErrorAndThrow("Entity {} has to implement EntityService", baseEntity.getTitle());
    }
    EntityService entityService = (EntityService) baseEntity;
    S service = (S) entityService.getService();
    if (!serviceClass.isAssignableFrom(service.getClass())) {
      logErrorAndThrow(
          "Entity {} has no service {}", baseEntity.getTitle(), serviceClass.getSimpleName());
    }
    return service;
  }

  default <T extends BaseEntity> T getMenuValueEntityRequired(
      String key, MenuBlock.ServerMenuBlock menuBlock) {
    String entityID = getMenuValue(key, menuBlock, String.class);
    if ("-".equals(entityID)) {
      logErrorAndThrow("Menu entity not selected for block: {}", key);
    }
    T entity = context().db().get(entityID);
    if (entity == null) {
      logErrorAndThrow("Unable to find entity for block: {}. Value: {}", key, entityID);
    }

    return entity;
  }

  default <P> P getMenuValue(String key, MenuBlock.StaticMenuBlock<P> menuBlock) {
    return getMenuValue(key, menuBlock, menuBlock.getTypeClass());
  }

  Map<String, JSONArray> getInputs();

  String getOpcode();

  String findField(Predicate<String> predicate);

  String getField(String fieldName);

  boolean getFieldBoolean(String fieldName);

  String getFieldId(String fieldName);

  boolean hasField(String fieldName);

  <T> T getSetting(Class<T> settingClass);

  void setValue(String key, State value);

  default State setValue(State value) {
    setValue("value", value);
    return value;
  }

  void handle();

  @SneakyThrows
  default void handleNext(ThrowingConsumer<WorkspaceBlock, Exception> nextConsumer) {
    nextConsumer.accept(getNextOrThrow());
  }

  @SneakyThrows
  default void handleNextOptional(ThrowingConsumer<WorkspaceBlock, Exception> nextConsumer) {
    WorkspaceBlock next = getNext();
    if (next != null) {
      nextConsumer.accept(next);
    }
  }

  @SneakyThrows
  default void handleChildOptional(ThrowingConsumer<WorkspaceBlock, Exception> childConsumer) {
    if (hasChild()) {
      childConsumer.accept(getChild());
    }
  }

  @SneakyThrows
  default void handleAndRelease(
      ThrowingRunnable<Exception> runHandler, ThrowingRunnable<Exception> releaseHandler) {
    runHandler.run();
    onRelease(releaseHandler);
  }

  default <T> void subscribeToLock(Lock lock, Runnable handler) {
    subscribeToLock(lock, o -> true, 0, null, handler);
  }

  default <T> void subscribeToLock(Lock lock, int timeout, TimeUnit timeUnit, Runnable handler) {
    subscribeToLock(lock, o -> true, timeout, timeUnit, handler);
  }

  default void subscribeToLock(Lock lock, Function<Object, Boolean> checkFn, Runnable handler) {
    subscribeToLock(lock, checkFn, 0, null, handler);
  }

  default void subscribeToLock(
      Lock lock,
      Function<Object, Boolean> checkFn,
      int timeout,
      TimeUnit timeUnit,
      Runnable runnable) {
    while (!Thread.currentThread().isInterrupted() && !this.isDestroyed()) {
      if (lock.await(this, timeout, timeUnit) && checkFn.apply(lock.getValue())) {
        if (!Thread.currentThread().isInterrupted() && !this.isDestroyed()) {
          try {
            runnable.run();
          } catch (Exception ex) {
            logError(CommonUtils.getErrorMessage(ex));
          } finally {
            setActiveWorkspace();
          }
        }
      }
    }
  }

  void setActiveWorkspace();

  default <T> void waitForLock(Lock lock, Runnable handler) {
    waitForLock(lock, 0, null, handler);
  }

  default <T> void waitForLock(Lock lock, int timeout, TimeUnit timeUnit, Runnable handler) {
    if (!Thread.currentThread().isInterrupted() && !this.isDestroyed()) {
      if (lock.await(this, timeout, timeUnit)) {
        if (!Thread.currentThread().isInterrupted() && !this.isDestroyed()) {
          try {
            handler.run();
          } catch (Exception ex) {
            logError(CommonUtils.getErrorMessage(ex));
          }
        }
      }
    }
  }

  State evaluate();

  default Integer getInputInteger(String key) {
    return getInputInteger(key, 0);
  }

  default Integer getInputInteger(String key, Integer defaultValue) {
    Float value = getInputFloat(key, null);
    return value == null ? defaultValue : value.intValue();
  }

  default Integer getInputIntegerRequired(String key) {
    return getInputFloatRequired(key, "(" + key + ") is mandatory field").intValue();
  }

  default Float getInputFloat(String key) {
    return getInputFloat(key, 0F);
  }

  Float getInputFloat(String key, Float defaultValue);

  default Float getInputFloatRequired(String key, String errorMessage) {
    Float value = getInputFloat(key);
    if (value == null) {
      logErrorAndThrow(errorMessage);
    }
    return value;
  }

  default String getInputString(String key) {
    return getInputString(key, "");
  }

  default String getInputStringRequired(String key) {
    return getInputStringRequired(key, "(" + key + ") is mandatory field");
  }

  default String getInputStringRequiredWithContext(String key) {
    return getInputStringRequiredWithContext(key, "(" + key + ") is mandatory field");
  }

  default String getInputStringRequiredWithContext(String key, String errorMessage) {
    String value = getInputString(key, null);
    if (value == null) {
      logErrorAndThrow(errorMessage);
    } else {
      value = evalStringWithContext(value, text -> String.valueOf(getValue(text)));
    }
    return value;
  }

  default String getInputStringRequired(String key, String errorMessage) {
    String value = getInputString(key, null);
    if (value == null || value.isEmpty()) {
      logErrorAndThrow(errorMessage);
    }
    return value;
  }

  default byte[] getInputByteArray(String key) {
    return getInputByteArray(key, new byte[0]);
  }

  byte[] getInputByteArray(String key, byte[] defaultValue);

  String getInputString(String key, String defaultValue);

  State getValue(String key);

  default JSONObject getInputJSON(String key) {
    return getInputJSON(key, null);
  }

  JSONObject getInputJSON(String key, JSONObject defaultValue);

  boolean getInputBoolean(String key);

  WorkspaceBlock getInputWorkspaceBlock(String key);

  Object getInput(String key, boolean fetchValue);

  boolean hasInput(String key);

  default boolean hasChild() {
    return hasInput("SUBSTACK");
  }

  default WorkspaceBlock getChild() {
    return getInputWorkspaceBlock("SUBSTACK");
  }

  String getId();

  String getExtensionId();

  default String getBlockId() {
    return getExtensionId() + "_" + getOpcode();
  }

  /**
   * @return next block
   */
  WorkspaceBlock getNext();

  WorkspaceBlock getParent();

  default WorkspaceBlock getTop() {
    WorkspaceBlock parent = getParent();
    if (parent != null) {
      return parent.getTop();
    }
    return this;
  }

  boolean isTopLevel();

  boolean isShadow();

  String getDescription();

  /**
   * Get Lock manager that wait when event occurs
   *
   * @return lock manager
   */
  LockManager getLockManager();

  boolean isDestroyed();

  void setThreadContext(ContextBGP.ThreadContext<?> threadContext);

  Context context();

  /**
   * Fires when current thread is released (i.e. when whole block scope is destroyed)
   *
   * @param listener - listener that fires on thread release
   */
  void onRelease(ThrowingRunnable<Exception> listener);

  /**
   * Get next block or throw error
   *
   * @return next block
   */
  default @NotNull WorkspaceBlock getNextOrThrow() {
    WorkspaceBlock next = getNext();
    if (next == null) {
      logErrorAndThrow("No next block found");
      throw new ServerException("No next block found");
    }
    return next;
  }

  default @NotNull WorkspaceBlock getChildOrThrow() {
    if (!hasChild()) {
      logErrorAndThrow("No child block found");
      throw new ServerException("No child block found");
    }
    return getChild();
  }

  default RawType getInputRawType(String key) {
    return getInputRawType(key, 10 * 1024 * 1024); // 10mb by default
  }

  @SneakyThrows
  default RawType getInputRawType(String key, int maxDownloadSize) {
    Object input = getInput(key, true);
    byte[] content = null;
    String name = null;
    if (input instanceof String mediaURL) {
      if (mediaURL.startsWith("http")) {
        // max 10mb
        return new RawType(Curl.download(mediaURL, maxDownloadSize));
      } else if (Files.isRegularFile(Paths.get(mediaURL))) {
        Path path = Paths.get(mediaURL);
        content = Files.readAllBytes(path);
        name = path.getFileName().toString();
      } /*else if (mediaURL.startsWith("data:")) {
            String mediaStringValue = mediaURL;
            if (mediaURL.startsWith("data:")) { // support data URI scheme
                String[] urlParts = mediaURL.split(",");
                if (urlParts.length > 1) {
                    mediaStringValue = urlParts[1];
                }
            }
            InputStream is = Base64.getDecoder().wrap(new ByteArrayInputStream(mediaStringValue.getBytes(StandardCharsets
            .UTF_8)));
            content = IOUtils.toByteArray(is);
        }*/ else {
        content = mediaURL.getBytes();
      }
    } else if (input instanceof RawType) {
      return (RawType) input;
    } else if (input instanceof byte[]) {
      content = (byte[]) input;
    } else if (input != null) {
      content = input.toString().getBytes();
    }

    if (content != null) {
      return new RawType(content).setName(name);
    }
    return null;
  }
}
