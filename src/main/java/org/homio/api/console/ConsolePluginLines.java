package org.homio.api.console;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ConsolePluginLines extends ConsolePlugin<Collection<String>> {

    @Override
    default @NotNull RenderType getRenderType() {
        return RenderType.string;
    }
}
