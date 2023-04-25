package org.homio.bundle.api.entity;

import java.util.Set;

public interface UserEntity {

    String getEmail();

    String getName();

    String getLang();

    UserType getUserType();

    Set<String> getRoles();

    default boolean isAdmin() {
        return getUserType() == UserType.ADMIN;
    }

    // other is not for homio user but other purposes
    enum UserType {
        ADMIN, PRIVILEGED, GUEST, OTHER
    }
}
