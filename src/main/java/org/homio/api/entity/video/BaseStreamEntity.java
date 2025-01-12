package org.homio.api.entity.video;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.BaseEntityIdentifier;
import org.homio.api.stream.StreamPlayer;
import org.jetbrains.annotations.NotNull;

public interface BaseStreamEntity extends BaseEntityIdentifier {
  @NotNull
  @JsonIgnore
  StreamPlayer getStreamPlayer();

  @NotNull
  @JsonIgnore
  BaseEntity getStreamEntity();
}
