package org.homio.api;

import lombok.SneakyThrows;
import org.homio.api.entity.UserEntity;
import org.homio.api.exception.NotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import static org.homio.api.entity.HasJsonData.LIST_DELIMITER;

public interface ContextUser {

  @NotNull
  Context context();

  /**
   * Does device has primary user and user/password is set
   */
  boolean isRequireAuth();

  void assertUserCredentials(String username, String password);

  boolean isAdminLoggedUser();

  @SneakyThrows
  default void assertAdminAccess() {
    if (!isAdminLoggedUser()) {
      throw new IllegalAccessException();
    }
  }

  default @NotNull UserEntity getLoggedInUserRequire() {
    UserEntity user = getLoggedInUser();
    if (user == null) {
      throw new NotFoundException("Unable to find authenticated user");
    }
    return user;
  }

  default @Nullable UserEntity getLoggedInUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      if (authentication instanceof AnonymousAuthenticationToken) {
        return null;
      }
      User user = (User) authentication.getPrincipal();
      String userEntityID = user.getUsername().split(LIST_DELIMITER)[0];
      return context().db().get(userEntityID);
    }
    return null;
  }
}
