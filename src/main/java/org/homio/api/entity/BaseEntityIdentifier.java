package org.homio.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.homio.api.Context;
import org.homio.api.model.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public interface BaseEntityIdentifier extends EntityFieldMetadata, Serializable {

  @JsonIgnore
  String getDefaultName();

  Context context();

  default @NotNull String getTitle() {
    return defaultIfBlank(getName(), defaultIfBlank(getDefaultName(), getEntityID()));
  }

  @Nullable String getName();

  @JsonIgnore
  default @Nullable String refreshName() {
    return getDefaultName();
  }

  @JsonIgnore
  @NotNull String getEntityPrefix();

  /**
   * Specify entity font awesome icon for UI purposes
   */
  default @Nullable Icon getEntityIcon() {
    return null;
  }

  /**
   * Specify type for dynamic update. For widget it will be always: 'widget', etc...
   *
   * @return - dynamic widget type
   */
  @JsonIgnore
  default @NotNull String getDynamicUpdateType() {
    return getType();
  }

  default void afterFetch() {

  }

  default void beforeDelete() {

  }

  default void afterDelete() {

  }

  // fires before persist/update
  default void validate() {

  }

  // calls before entity inserted into db
  default void beforePersist() {

  }

  // calls after entity inserted into db
  default void afterPersist() {

  }

  // calls before update updated into db
  default void beforeUpdate() {

  }

  // calls after update updated into db
  default void afterUpdate() {

  }
}
