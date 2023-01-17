package org.touchhome.bundle.api.console;

import java.util.List;
import org.touchhome.bundle.api.entity.TreeConfiguration;

public interface ConsolePluginTree extends ConsolePlugin<List<TreeConfiguration>> {

    @Override
    default RenderType getRenderType() {
        return RenderType.tree;
    }
}
