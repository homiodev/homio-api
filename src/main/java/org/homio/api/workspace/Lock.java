package org.homio.api.workspace;

import java.util.concurrent.TimeUnit;

public interface Lock {

  Object getValue();

  /**
   * Await for events from lock.
   *
   * @param workspaceBlock -
   * @return true if successfully returned, false in case of exception/interruption
   */
  default boolean await(WorkspaceBlock workspaceBlock) {
    return await(workspaceBlock, 0, null);
  }

  boolean await(WorkspaceBlock workspaceBlock, int timeout, TimeUnit timeUnit);

  // signal to all locks with no value
  default void signalAll() {
    signalAll(null);
  }

  // signal to all locks with specified value
  void signalAll(Object value);

  void addReleaseListener(String key, Runnable listener);
}
