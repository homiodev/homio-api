package org.homio.bundle.api.console;

import java.util.List;
import org.homio.bundle.api.fs.TreeConfiguration;

public interface ConsolePluginTree extends ConsolePlugin<List<TreeConfiguration>> {

    @Override
    default RenderType getRenderType() {
        return RenderType.tree;
    }
}
