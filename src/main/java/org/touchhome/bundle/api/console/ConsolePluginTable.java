package org.touchhome.bundle.api.console;

import java.util.Collection;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

public interface ConsolePluginTable<T extends HasEntityIdentifier>
        extends ConsolePlugin<Collection<T>> {

    @Override
    default RenderType getRenderType() {
        return RenderType.table;
    }

    Class<T> getEntityClass();
}
