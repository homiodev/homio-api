package org.touchhome.bundle.api.workspace;

import org.touchhome.bundle.api.scratch.WorkspaceBlock;

import java.util.function.Supplier;

public interface BroadcastLockManager {

    void signalAll(String key, Object value);

    default void signalAll(String key) {
        signalAll(key, null);
    }

    BroadcastLock getOrCreateLock(WorkspaceBlock workspaceBlock);

    BroadcastLock getOrCreateLock(WorkspaceBlock workspaceBlock, String key);

    BroadcastLock getOrCreateLock(WorkspaceBlock workspaceBlock, String key, Object expectedValue);

    BroadcastLock listenEvent(WorkspaceBlock workspaceBlock, Supplier<Boolean> supplier);
}
