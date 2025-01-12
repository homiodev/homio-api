package org.homio.api.entity.log;

import org.homio.api.entity.HasJsonData;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;

public interface HasEntitySourceLog extends HasJsonData {

  /**
   * Return list of sources that able to select to fetch logs from.
   *
   * @return List of sources
   */
  @NotNull List<OptionModel> getLogSources();

  @Nullable InputStream getSourceLogInputStream(@NotNull String sourceID) throws Exception;
}

