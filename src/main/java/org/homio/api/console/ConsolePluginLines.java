package org.homio.api.console;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public interface ConsolePluginLines extends ConsolePlugin<Collection<String>> {

  @Override
  default @NotNull RenderType getRenderType() {
    return RenderType.string;
  }
}
