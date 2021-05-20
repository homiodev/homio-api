package org.touchhome.bundle.api.workspace;

import com.pivovarit.function.ThrowingConsumer;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.BaseEntity;
import org.touchhome.bundle.api.state.RawType;
import org.touchhome.bundle.api.util.Curl;
import org.touchhome.bundle.api.workspace.scratch.MenuBlock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface WorkspaceBlock {
    Set<String> MEDIA_EXTENSIONS = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".jpe", ".jif", ".jfif",
            ".jfi", ".webp", ".webm", ".mkv", ".flv", ".vob", ".ogv", ".ogg", ".drc", ".avi", ".wmv", ".mp4", ".mpg",
            ".mpeg", ".m4v", ".flv", ".xlsx", ".xltx", ".xls", ".xlt", ".xml", ".json", ".txt", ".csv", ".pdf", ".htm",
            ".html", ".7z", ".zip", ".tar.gz", ".gz", ".js", ".mp3"));

    void logError(String message, Object... params);

    void logErrorAndThrow(String message, Object... params);

    void logWarn(String message, Object... params);

    void logInfo(String message, Object... params);

    <P> P getMenuValue(String key, MenuBlock menuBlock, Class<P> type);

    default <P> List<P> getMenuValues(String key, MenuBlock menuBlock, Class<P> type) {
        return getMenuValues(key, menuBlock, type, "~~~");
    }

    <P> List<P> getMenuValues(String key, MenuBlock menuBlock, Class<P> type, String delimiter);

    default String getMenuValue(String key, MenuBlock.ServerMenuBlock menuBlock) {
        return getMenuValue(key, menuBlock, String.class);
    }

    default <T extends BaseEntity> T getMenuValueEntity(String key, MenuBlock.ServerMenuBlock menuBlock) {
        return getEntityContext().getEntity(getMenuValue(key, menuBlock, String.class));
    }

    default <T extends BaseEntity> T getMenuValueEntityRequired(String key, MenuBlock.ServerMenuBlock menuBlock) {
        String entityID = getMenuValue(key, menuBlock, String.class);
        T entity = getEntityContext().getEntity(entityID);
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

    String getFieldId(String fieldName);

    boolean hasField(String fieldName);

    void setValue(Object value);

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
        WorkspaceBlock child = getChild();
        if (child != null) {
            childConsumer.accept(child);
        }
    }

    default <T> void subscribeToLock(BroadcastLock lock, Runnable handler) {
        subscribeToLock(lock, o -> true, 0, null, handler);
    }

    default <T> void subscribeToLock(BroadcastLock lock, int timeout, TimeUnit timeUnit, Runnable handler) {
        subscribeToLock(lock, o -> true, timeout, timeUnit, handler);
    }

    default void subscribeToLock(BroadcastLock lock, Function<Object, Boolean> checkFn, Runnable handler) {
        subscribeToLock(lock, checkFn, 0, null, handler);
    }

    default void subscribeToLock(BroadcastLock lock, Function<Object, Boolean> checkFn, int timeout, TimeUnit timeUnit, Runnable runnable) {
        while (!Thread.currentThread().isInterrupted() && !this.isDestroyed()) {
            if (lock.await(this, timeout, timeUnit) && checkFn.apply(lock.getValue())) {
                if (!Thread.currentThread().isInterrupted() && !this.isDestroyed()) {
                    runnable.run();
                }
            }
        }
    }

    default <T> void waitForLock(BroadcastLock lock, Runnable handler) {
        waitForLock(lock, 0, null, handler);
    }

    default <T> void waitForLock(BroadcastLock lock, int timeout, TimeUnit timeUnit, Runnable handler) {
        if (!Thread.currentThread().isInterrupted() && !this.isDestroyed()) {
            if (lock.await(this, timeout, timeUnit)) {
                if (!Thread.currentThread().isInterrupted() && !this.isDestroyed()) {
                    handler.run();
                }
            }
        }
    }

    Object evaluate();

    Integer getInputInteger(String key);

    Float getInputFloat(String key);

    default String getInputString(String key) {
        return getInputString(key, "");
    }

    default String getInputStringRequired(String key) {
        return getInputStringRequired(key, "<" + key + "> is mandatory field");
    }

    default String getInputStringRequired(String key, String errorMessage) {
        String value = getInputString(key);
        if (StringUtils.isEmpty(value)) {
            logErrorAndThrow(errorMessage);
        }
        return value;
    }

    default byte[] getInputByteArray(String key) {
        return getInputByteArray(key, new byte[0]);
    }

    byte[] getInputByteArray(String key, byte[] defaultValue);

    String getInputString(String key, String defaultValue);

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

    WorkspaceBlock getNext();

    WorkspaceBlock getParent();

    boolean isTopLevel();

    boolean isShadow();

    String getDescription();

    void setStateHandler(Consumer<String> stateHandler);

    void setState(String state);

    boolean isDestroyed();

    EntityContext getEntityContext();

    void onRelease(Runnable listener);

    default WorkspaceBlock getNextOrThrow() {
        WorkspaceBlock next = getNext();
        if (next == null) {
            logErrorAndThrow("No next block found");
        }
        return next;
    }

    default WorkspaceBlock getChildOrThrow() {
        WorkspaceBlock child = getChild();
        if (child == null) {
            logErrorAndThrow("No child block found");
        }
        return child;
    }

    default RawType getInputRawType(String key) {
        return getInputRawType(key, 10 * 1024 * 1024); // 10mb by default
    }

    @SneakyThrows
    default RawType getInputRawType(String key, int maxDownloadSize) {
        Object input = getInput(key, true);
        byte[] content = null;
        String name = null;
        if (input instanceof String) {
            String mediaURL = (String) input;
            if (mediaURL.startsWith("http")) {
                // max 10mb
                return Curl.download(mediaURL, maxDownloadSize);
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
                InputStream is = Base64.getDecoder().wrap(new ByteArrayInputStream(mediaStringValue.getBytes(StandardCharsets.UTF_8)));
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
