package org.homio.api.console;

import org.homio.api.console.ConsolePluginFrame.FrameConfiguration;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface ConsolePluginFrame extends ConsolePlugin<FrameConfiguration> {

  @Override
  default @NotNull RenderType getRenderType() {
    return RenderType.frame;
  }

  @Override
  default JSONObject getOptions() {
    return new JSONObject().put("host", getValue().host());
  }

  record FrameConfiguration(@NotNull String host) {}
}
