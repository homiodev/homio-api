package org.homio.api.console;

import org.homio.api.model.FileContentType;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface ConsolePluginString extends ConsolePlugin<String> {

    @Override
    default @NotNull RenderType getRenderType() {
        return RenderType.string;
    }

    FileContentType getContentType();

    @Override
    default JSONObject getOptions() {
        return new JSONObject().put("contentType", getContentType());
    }
}
