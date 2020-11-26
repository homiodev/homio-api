package org.touchhome.bundle.api.console;

public interface CommunicatorConsolePlugin extends LinesConsolePlugin {

    @Override
    default RenderType getRenderType() {
        return RenderType.comm;
    }
}
