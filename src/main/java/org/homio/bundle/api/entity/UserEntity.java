package org.homio.bundle.api.entity;

import java.util.Set;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

public interface UserEntity {

    @NotNull String getEntityID();

    String getEmail();

    String getName();

    @NotNull String getLang();

    @NotNull UserType getUserType();

    @NotNull Set<String> getRoles();

    /**
     * Log for specific user
     *
     * @param message - text
     * @param level   - level
     */
    void log(@NotNull String message, @NotNull Level level);

    default void logInfo(@NotNull String message) {
        log(message, Level.INFO);
    }

    default void logError(@NotNull String message) {
        log(message, Level.ERROR);
    }

    default boolean isAdmin() {
        return getUserType() == UserType.ADMIN;
    }

    // other is not for homio user but other purposes
    enum UserType {
        ADMIN, PRIVILEGED, GUEST, OTHER
    }

    default boolean isAllowResource(String resource) {
        return getUserType() == UserType.ADMIN;
    }
}
