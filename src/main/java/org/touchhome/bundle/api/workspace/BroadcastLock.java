package org.touchhome.bundle.api.workspace;

import org.touchhome.bundle.api.scratch.WorkspaceBlock;

public interface BroadcastLock<T> {

    T getValue();

    boolean await(WorkspaceBlock workspaceBlock);

    default void signalAll() {
        signalAll(null);
    }

    void signalAll(T value);

    void addReleaseListener(String key, Runnable listener);
}
