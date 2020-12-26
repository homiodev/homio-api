package org.touchhome.bundle.api.workspace;

import org.json.JSONArray;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.workspace.scratch.MenuBlock;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface WorkspaceBlock {
    void logError(String message, Object... params);

    void logErrorAndThrow(String message, Object... params);

    void logWarn(String message, Object... params);

    void logInfo(String message, Object... params);

    <P> P getMenuValue(String key, MenuBlock menuBlock, Class<P> type);

    default String getMenuValue(String key, MenuBlock.ServerMenuBlock menuBlock) {
        return getMenuValue(key, menuBlock, String.class);
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

    default <T> void subscribeToLock(BroadcastLock<T> lock) {
        subscribeToLock(lock, o -> true);
    }

    default <T> void subscribeToLock(BroadcastLock<T> lock, Function<T, Boolean> checkFn) {
        while (!Thread.currentThread().isInterrupted()) {
            if (lock.await(this) && checkFn.apply(lock.getValue())) {
                this.getNext().handle();
            }
        }
    }

    Object evaluate();

    Integer getInputInteger(String key);

    Float getInputFloat(String key);

    String getInputString(String key);

    boolean getInputBoolean(String key);

    WorkspaceBlock getInputWorkspaceBlock(String key);

    Object getInput(String key, boolean fetchValue);

    boolean hasInput(String key);

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
}
