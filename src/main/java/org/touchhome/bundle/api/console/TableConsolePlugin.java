package org.touchhome.bundle.api.console;

import org.touchhome.bundle.api.model.HasEntityIdentifier;

import java.util.Collection;

public interface TableConsolePlugin<T extends HasEntityIdentifier> extends ConsolePlugin<Collection<T>> {

    @Override
    default RenderType getRenderType() {
        return RenderType.table;
    }

    Class<T> getEntityClass();
}
