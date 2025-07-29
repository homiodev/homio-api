package org.homio.api.entity.video;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HasVideoSources {

  @JsonIgnore
  @NotNull
  List<OptionModel> getVideoSources();

  default @Nullable String getPrimaryVideoSource() {
    List<OptionModel> sources = getVideoSources();
    if (sources.isEmpty()) {
      return null;
    }
    OptionModel firstSource = sources.get(0);
    if (firstSource.hasChildren()) {
      List<OptionModel> models = Objects.requireNonNull(firstSource.getChildren());
      return firstSource.getKey() + "-->" + models.get(0).getKey();
    }
    return firstSource.getKey();
  }
}
