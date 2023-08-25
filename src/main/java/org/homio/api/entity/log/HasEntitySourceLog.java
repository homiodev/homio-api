package org.homio.api.entity.log;

import java.io.InputStream;
import java.util.List;
import org.homio.api.entity.HasJsonData;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;

public interface HasEntitySourceLog extends HasJsonData {

    /**
     * Return list of sources that able to select to fetch logs from.
     *
     * @return List of sources
     */
    @NotNull List<OptionModel> getLogSources();

    @NotNull InputStream getSourceLogInputStream(@NotNull String sourceID);
}

