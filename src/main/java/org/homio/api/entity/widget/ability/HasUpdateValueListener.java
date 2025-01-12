package org.homio.api.entity.widget.ability;

import org.homio.api.Context;
import org.homio.api.state.State;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.time.Duration;
import java.util.function.Consumer;

public interface HasUpdateValueListener {

  @NotNull
  UpdateValueListener addUpdateValueListener(@NotNull Context context,
                                             @NotNull String discriminator,
                                             @NotNull Duration ttl,
                                             @NotNull JSONObject dynamicParameters,
                                             @NotNull Consumer<State> listener);

  interface UpdateValueListener {
    // refresh ttl to allow receiving events
    void refresh();
  }
}
