package org.touchhome.bundle.api.console;

import org.json.JSONObject;

public interface ConsolePluginString extends ConsolePlugin<String> {

    @Override
    default RenderType getRenderType() {
        return RenderType.string;
    }

    ConsolePluginEditor.ContentType getContentType();

    @Override
    default JSONObject getOptions() {
        return new JSONObject().put("contentType", getContentType());
    }
}
