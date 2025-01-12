package org.homio.api.entity;

import lombok.SneakyThrows;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.UIFieldGroup;
import org.homio.api.ui.field.UIFieldTab;

import java.util.Set;

public interface HasPermissions extends HasJsonData, BaseEntityIdentifier {
  @UIField(order = 1, hideInView = true)
  @UIFieldGroup(order = 1, value = "ACCESS_HIDE", borderColor = "#5B69B4")
  @UIFieldTab(value = "PERMISSIONS", order = 99)
  default Set<String> getHideForUsers() {
    UserEntity user = context().user().getLoggedInUser();
    if (user != null && !user.isAdmin()) {
      return Set.of();
    }
    return getJsonDataSet("hide_4usr");
  }

  @SneakyThrows
  default void setHideForUsers(String value) {
    UserEntity user = this.context().user().getLoggedInUserRequire();
    if (!user.isAdmin()) {
      throw new IllegalAccessException("Only admin user able to modify entity permissions");
    }
    setJsonData("hide_4usr", value);
  }

  @UIField(order = 2, hideInView = true)
  @UIFieldGroup("ACCESS_HIDE")
  @UIFieldTab("PERMISSIONS")
  default Set<String> getDisableEditForUsers() {
    UserEntity user = context().user().getLoggedInUser();
    if (user != null && !user.isAdmin()) {
      return Set.of();
    }
    return getJsonDataSet("dd_usr");
  }

  @SneakyThrows
  default void setDisableEditForUsers(String value) {
    UserEntity user = this.context().user().getLoggedInUserRequire();
    if (!user.isAdmin()) {
      throw new IllegalAccessException("Only admin user able to modify entity permissions");
    }
    setJsonData("dd_usr", value);
  }
}
