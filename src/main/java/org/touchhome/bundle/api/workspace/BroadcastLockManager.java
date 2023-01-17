package org.touchhome.bundle.api.workspace;

import java.util.function.Supplier;

public interface BroadcastLockManager {

    /** Signal to all broadcast locks with specified key and value */
    void signalAll(String key, Object value);

    /** Signal to all broadcast locks with specified key and without any value */
    default void signalAll(String key) {
        signalAll(key, null);
    }

    BroadcastLock getOrCreateLock(WorkspaceBlock workspaceBlock);

    BroadcastLock getOrCreateLock(WorkspaceBlock workspaceBlock, String key);

    /**
     * Create lock.
     *
     * @param workspaceBlock
     * @param key
     * @param expectedValue - any value. If Pattern - than checks if value match pattern
     * @return
     */
    BroadcastLock getOrCreateLock(WorkspaceBlock workspaceBlock, String key, Object expectedValue);

    /**
     * Creates BroadcastLock and attach it to thread that check supplier once per second If supplier
     * return true - signal broadcast lock
     */
    BroadcastLock listenEvent(WorkspaceBlock workspaceBlock, Supplier<Boolean> supplier);
}
