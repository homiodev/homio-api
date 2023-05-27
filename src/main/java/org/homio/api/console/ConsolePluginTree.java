package org.homio.api.console;

import java.util.List;
import org.homio.api.fs.TreeConfiguration;

public interface ConsolePluginTree extends ConsolePlugin<List<TreeConfiguration>> {

    @Override
    default RenderType getRenderType() {
        return RenderType.tree;
    }
}
