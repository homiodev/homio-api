package org.homio.api.service.ssh;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public interface SshProviderService<T extends SshBaseEntity> {

  /**
   * Open ssh session
   *
   * @param sshEntity ssh entity that hold configuration
   * @return session token
   */
  @Nullable SshSession<T> openSshSession(@NotNull T sshEntity);

  /**
   * Fire execute command on remote shell
   *
   * @param command command to execute
   */
  void execute(@NotNull SshSession<T> sshSession, @NotNull String command);

  /**
   * Close ssh session
   */
  void closeSshSession(@Nullable SshSession<T> sshSession);

  default void resizeSshConsole(@NotNull SshSession<T> sshSession, int cols) {

  }

  @Getter
  @Setter
  @ToString(exclude = "entity")
  @RequiredArgsConstructor
  class SshSession<T extends SshBaseEntity> {

    /**
     * Unique token for session
     */
    private final String token;

    /**
     * Web socker url
     */
    private final String wsURL;

    @JsonIgnore
    private final @NotNull T entity;

    @JsonIgnore
    private final @NotNull Map<String, String> metadata = new HashMap<>();
  }
}
