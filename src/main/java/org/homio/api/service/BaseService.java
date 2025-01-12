package org.homio.api.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BaseService {
  @NotNull String getEntityID();

  @NotNull String getName();

  @Nullable String getParent();

  @Nullable String getIcon();

  @Nullable String getColor();

  boolean isExposeService();
}
