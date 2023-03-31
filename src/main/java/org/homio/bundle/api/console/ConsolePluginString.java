package org.homio.bundle.api.console;

import org.homio.bundle.api.model.FileContentType;
import org.json.JSONObject;

public interface ConsolePluginString extends ConsolePlugin<String> {

    @Override
    default RenderType getRenderType() {
        return RenderType.string;
    }

    FileContentType getContentType();

    @Override
    default JSONObject getOptions() {
        return new JSONObject().put("contentType", getContentType());
    }
}
