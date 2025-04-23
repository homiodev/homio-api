package org.homio.api.workspace;

import java.util.function.Supplier;

public interface LockManager {

  /**
   * Signal to all broadcast locks with specified key and value
   *
   * @param key -
   * @param value -
   */
  void signalAll(String key, Object value);

  /**
   * Signal to all broadcast locks with specified key and without any value
   *
   * @param key -
   */
  default void signalAll(String key) {
    signalAll(key, null);
  }

  Lock getLock(WorkspaceBlock workspaceBlock);

  Lock getLock(WorkspaceBlock workspaceBlock, String key);

  /**
   * Create lock.
   *
   * @param workspaceBlock -
   * @param key -
   * @param expectedValue - any value. If Pattern - than checks if value match pattern
   * @return -
   */
  Lock getLock(WorkspaceBlock workspaceBlock, String key, Object expectedValue);

  /**
   * Creates BroadcastLock and attach it to thread that check supplier once per second If supplier
   * return true - signal broadcast lock
   *
   * @param workspaceBlock -
   * @param supplier -
   * @return - BroadcastLock
   */
  Lock listenEvent(WorkspaceBlock workspaceBlock, Supplier<Boolean> supplier);
}
