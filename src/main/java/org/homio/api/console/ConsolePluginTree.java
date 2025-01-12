package org.homio.api.console;

import org.homio.api.fs.TreeConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ConsolePluginTree extends ConsolePlugin<List<TreeConfiguration>> {

  @Override
  default @NotNull RenderType getRenderType() {
    return RenderType.tree;
  }
}
