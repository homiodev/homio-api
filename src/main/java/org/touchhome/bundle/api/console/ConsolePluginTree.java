package org.touchhome.bundle.api.console;

import org.touchhome.bundle.api.entity.TreeConfiguration;

import java.util.List;

public interface ConsolePluginTree extends ConsolePlugin<List<TreeConfiguration>> {

    @Override
    default RenderType getRenderType() {
        return RenderType.tree;
    }
}
