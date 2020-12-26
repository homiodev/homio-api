package org.touchhome.bundle.api.workspace.scratch;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Components that extend this interface able to executes on workspace
 */
public interface Scratch3OtherBlocksHolder {
    List<Scratch3Block> getScratch3Blocks();

    default Map<? extends String, ? extends MenuBlock> createScratch3Menus() {
        return Collections.emptyMap();
    }
}
