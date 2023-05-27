package org.homio.api.console;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

public interface ConsolePluginFrame extends ConsolePlugin<ConsolePluginFrame.FrameConfiguration> {

    @Override
    default RenderType getRenderType() {
        return RenderType.frame;
    }

    @Override
    default JSONObject getOptions() {
        return new JSONObject().put("host", getValue().getHost());
    }

    @Getter
    @RequiredArgsConstructor
    class FrameConfiguration {
        private final String host;
    }
}
