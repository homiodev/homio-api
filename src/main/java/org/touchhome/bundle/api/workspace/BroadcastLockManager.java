package org.touchhome.bundle.api.workspace;

import java.util.function.Supplier;

public interface BroadcastLockManager {

    /**
     * Signal to all broadcast locks with specified key and value
     */
    void signalAll(String key, Object value);

    /**
     * Signal to all broadcast locks with specified key and without any value
     */
    default void signalAll(String key) {
        signalAll(key, null);
    }

    <T> BroadcastLock<T> getOrCreateLock(WorkspaceBlock workspaceBlock);

    <T> BroadcastLock<T> getOrCreateLock(WorkspaceBlock workspaceBlock, String key);

    <T> BroadcastLock<T> getOrCreateLock(WorkspaceBlock workspaceBlock, String key, T expectedValue);

    /**
     * Creates BroadcastLock and attach it to thread that check supplier once per second
     * If supplier return true - signal broadcast lock
     */
    <T> BroadcastLock<T> listenEvent(WorkspaceBlock workspaceBlock, Supplier<Boolean> supplier);
}
