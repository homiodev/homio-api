package org.touchhome.bundle.api.console;

import org.json.JSONObject;
import org.touchhome.bundle.api.model.FileContentType;

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
