package org.touchhome.bundle.api.console;

public interface StringConsolePlugin extends ConsolePlugin<String> {

    @Override
    default RenderType getRenderType() {
        return RenderType.string;
    }


    enum StringRenderType {
        Plain, Markdown, HTML,
    }
}
