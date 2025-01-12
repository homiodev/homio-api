package org.homio.api.ui.field.selection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.homio.api.model.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SelectionConfiguration {

  @JsonIgnore
  @NotNull Icon getSelectionIcon();

  @JsonIgnore
  default @Nullable String getSelectionDescription() {
    return null;
  }
}
