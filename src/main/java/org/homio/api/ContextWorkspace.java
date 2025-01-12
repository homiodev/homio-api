package org.homio.api;

import org.homio.api.workspace.scratch.Scratch3ExtensionBlocks;
import org.jetbrains.annotations.NotNull;

public interface ContextWorkspace {

  @NotNull Context context();

  /**
   * Register custom Scratch3Extension
   *
   * @param scratch3ExtensionBlocks - dynamic block to register
   */
  void registerScratch3Extension(@NotNull Scratch3ExtensionBlocks scratch3ExtensionBlocks);
}
